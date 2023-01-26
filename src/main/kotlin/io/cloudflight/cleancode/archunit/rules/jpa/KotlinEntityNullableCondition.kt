package io.cloudflight.cleancode.archunit.rules.jpa

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

/**
 * This condition ensures that all members of a Kotlin JPA entity match their kotlin nullability flag with the according
 * specification of JPA columns
 */
internal class KotlinEntityNullableCondition :
    ArchCondition<JavaClass>("have only members where the nullable flag of kotlin matches the JPA column specification") {

    override fun check(item: JavaClass, events: ConditionEvents) {
        // we check if it is either a ManyToMany or OneToMany relation, we do not check nullability here
        item.reflect().kotlin.memberProperties
            .filter { !isCollectionType(it.returnType.javaType) && !isEmbeddable(it) }
            .forEach { checkMemberPropertyNullability(item, events, it) }
    }


    private fun checkMemberPropertyNullability(
        item: JavaClass,
        events: ConditionEvents,
        memberProperty: KProperty1<out Any, Any?>
    ) {
        val jpaModelNullable = getJpaNullableValue(memberProperty)

        fun ifNotNullableInDB() = takeIf { !jpaModelNullable }
            ?.let { "[${item.name}]: ${memberProperty.name} is a field in a single-table inheritance subclass, but it is marked as not nullable in the DB" }

        fun ifNotNullInKotlinButInDB() = takeIf { jpaModelNullable && !memberProperty.returnType.isMarkedNullable }
            ?.let { "[${item.name}]: ${memberProperty.name} is nullable in the DB, but the kotlin field is not nullable" }

        fun ifNullInKotlinButNotInDB() = takeIf { !jpaModelNullable && memberProperty.returnType.isMarkedNullable }
            ?.let { "[${item.name}]: ${memberProperty.name} is not nullable in the DB, but the kotlin field is nullable" }


        when (isFieldInSingleTableInheritanceSubclass(memberProperty)) {
            true -> ifNotNullableInDB()
            false -> ifNotNullInKotlinButInDB() ?: ifNullInKotlinButNotInDB()
        }?.let { events.add(SimpleConditionEvent(memberProperty, false, it)) }
    }

    private fun getJpaNullableValue(memberProperty: KProperty1<out Any, Any?>): Boolean {
        var nullable = defaultNullableFlagForType(memberProperty)

        findAnnotation<NotNull>(memberProperty) { false }?.let { nullable = it }
        findAnnotation<NotBlank>(memberProperty) { false }?.let { nullable = it }
        findAnnotation<NotEmpty>(memberProperty) { false }?.let { nullable = it }
        findAnnotation<Id>(memberProperty) { false }?.let { nullable = it }
        findAnnotation<GeneratedValue>(memberProperty) { nullableIfNotTypeLong(memberProperty) }?.let { nullable = it }
        findAnnotation<ManyToOne>(memberProperty) { manyToOne -> manyToOne.optional }?.let { nullable = it }
        return nullable
    }

    private inline fun <reified T : Annotation> findAnnotation(property: KProperty<Any?>, function: (T) -> Boolean) =
        property.javaField?.getAnnotation(T::class.java)?.let { function(it) }

    private fun nullableIfNotTypeLong(property: KProperty<Any?>): Boolean =
        !property.returnType.javaType.typeName.lowercase().contains("long")


    private fun isEmbeddable(memberProperty: KProperty1<out Any, Any?>): Boolean {
        return memberProperty.javaField?.getAnnotation(jakarta.persistence.EmbeddedId::class.java) != null
                || memberProperty.javaField?.getAnnotation(jakarta.persistence.Embedded::class.java) != null
    }

    private fun isCollectionType(javaClass: Type): Boolean {
        return (javaClass as? ParameterizedType)
            ?.let { it.rawType as? Class<*> }
            ?.let { java.util.Collection::class.java.isAssignableFrom(it) }
            ?: false
    }

    private fun isFieldInSingleTableInheritanceSubclass(memberProperty: KProperty1<out Any, Any?>): Boolean {
        return (memberProperty.instanceParameter?.type?.javaType as? Class<*>)
            ?.takeIf { it.kotlin.declaredMemberProperties.contains(memberProperty) }
            ?.let { anySuperclassWithInheritanceTypeSingleTable(it) }
            ?: false
    }

    private fun anySuperclassWithInheritanceTypeSingleTable(declaringType: Class<*>): Boolean =
        declaringType.kotlin.allSuperclasses.any { superclass ->
            superclass.findAnnotation<Inheritance>()
                ?.let { it.strategy == InheritanceType.SINGLE_TABLE }
                ?: false
        }

    /**
     * Hibernate has some fancy logic what the default nullable value is depending on the underlying type
     */
    private fun defaultNullableFlagForType(memberProperty: KProperty1<out Any, Any?>): Boolean {
        if (isFieldInSingleTableInheritanceSubclass(memberProperty)) return true
        return when (memberProperty.returnType.javaType) {
            Boolean::class.javaPrimitiveType -> false
            Int::class.javaPrimitiveType -> false
            Long::class.javaPrimitiveType -> false
            else -> true
        }
    }
}
