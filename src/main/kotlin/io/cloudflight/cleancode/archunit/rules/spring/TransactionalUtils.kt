package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.*
import com.tngtech.archunit.core.domain.properties.HasName
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import org.springframework.data.repository.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class NotBeTransactional :
    ArchCondition<JavaCodeUnit>("not be annotated with @Transactional") {
    override fun check(item: JavaCodeUnit, events: ConditionEvents) {
        if (TransactionalHelper.isTransactional(item, checkOwner = true)) {
            events.add(
                SimpleConditionEvent.violated(
                    item,
                    "$item should not be annotated with @Transactional as it is a controller method"
                )
            )
        }
    }
}


class BeMemberOfInnerOrAnonymousClass : ArchCondition<JavaMethod>("be member of an inner or anonymous class") {
    override fun check(item: JavaMethod, events: ConditionEvents) {
        if (!item.owner.isInnerClass && !item.owner.isAnonymousClass && !item.owner.isInterface) {
            events.add(
                SimpleConditionEvent.violated(
                    item,
                    "$item is not from an inner class"
                )
            )
        }
    }
}

class BeDefaultMethod : ArchCondition<JavaMethod>("be a default method") {
    override fun check(item: JavaMethod, events: ConditionEvents) {
        if (!item.name.endsWith("\$default")) {  // do not check default methods
            events.add(
                SimpleConditionEvent.violated(
                    item,
                    "$item is not a default method"
                )
            )
        }
    }
}

class AreTransactional : DescribedPredicate<JavaMethod>("are @Transactional") {
    override fun test(input: JavaMethod): Boolean =
        !input.modifiers.contains(JavaModifier.STATIC)
                && !input.modifiers.contains(JavaModifier.PRIVATE)
                && TransactionalHelper.isTransactional(input)
}


object JavaMethodHelper {
    fun collectTransitiveCalls(
        method: JavaMethod,
        breakOnTransactional: Boolean = false
    ): List<JavaCodeUnit> {
        val list = mutableListOf<JavaCodeUnit>()
        collectTransitiveCalls(method, list, breakOnTransactional)
        return list
    }

    private fun collectTransitiveCalls(
        method: JavaCodeUnit,
        list: MutableList<JavaCodeUnit>,
        breakOnTransactional: Boolean
    ) {
        val currentMethodCalls = method.getMethodCalls().mapNotNull { it.resolveMember().orElse(null) }

        for (javaMethodCall in method.getMethodCalls()) {
            for (javaCodeUnit in javaMethodCall.filterForCallLoops(list, currentMethodCalls)) {
                list.add(javaCodeUnit)
                if (breakOnTransactional && TransactionalHelper.isTransactional(javaCodeUnit)) continue
                collectTransitiveCalls(javaCodeUnit, list, breakOnTransactional)
            }
        }
    }

    private fun JavaCodeUnit.getMethodCalls(): List<AccessTarget.CodeUnitCallTarget> =
        this.callsFromSelf.map { it.target }

    private fun AccessTarget.CodeUnitCallTarget.filterForCallLoops(
        list: MutableList<JavaCodeUnit>,
        currentMethodCalls: List<JavaCodeUnit>
    ): List<JavaCodeUnit> {
        val member = this.resolveMember()
        if (member.isPresent) {
            return listOf(member.get()).filter { javaCodeUnit ->
                !list.contains(javaCodeUnit) ||
                        (currentMethodCalls.count { it == javaCodeUnit } > 1
                                && list.count { it == javaCodeUnit } < currentMethodCalls.count { it == javaCodeUnit }
                                )

            }
        } else {
            return emptyList()
        }
    }

}


object TransactionalHelper {
    fun isTransactional(accessTarget: AccessTarget): Boolean {
        return if (accessTarget.resolveMember().isPresent) {
            (accessTarget is AccessTarget.MethodCallTarget
                    && isTransactional(accessTarget.resolveMember().get(), checkOwner = true, checkSubClasses = true))
        } else {
            false
        }
    }


    @Suppress("ReturnCount")
    fun isTransactional(
        codeUnit: JavaCodeUnit,
        checkOwner: Boolean = false,
        checkSubClasses: Boolean = false
    ): Boolean {
        if (codeUnit.isAnnotatedOrMetaAnnotatedWithTransactional()) return true
        if (checkOwner && codeUnit.isOwnerAnnotatedOrMetaAnnotatedWithTransactional()) return true

        return codeUnit.takeIf { checkSubClasses && it is JavaMethod }
            ?.let { it as JavaMethod }
            ?.checkOtherImplementationsIfTransactional(checkOwner) ?: false
    }

    fun isTransactional(codeUnit: JavaClass): Boolean =
        codeUnit.isAnnotatedOrMetaAnnotatedWithTransactional()

    private fun JavaCodeUnit.isAnnotatedOrMetaAnnotatedWithTransactional() =
        this.isAnnotatedWith(TransactionalAnnotation()) || this.isMetaAnnotatedWith(TransactionalAnnotation())


    private fun JavaCodeUnit.isOwnerAnnotatedOrMetaAnnotatedWithTransactional() =
        this.owner.isAnnotatedWith(TransactionalAnnotation()) || this.owner.isMetaAnnotatedWith(TransactionalAnnotation())

    private fun JavaClass.isAnnotatedOrMetaAnnotatedWithTransactional() =
        this.isAnnotatedWith(TransactionalAnnotation()) || this.isMetaAnnotatedWith(TransactionalAnnotation())

    class TransactionalAnnotation : DescribedPredicate<JavaAnnotation<*>>("") {
        override fun test(input: JavaAnnotation<*>): Boolean =
            input.toAnnotation(Transactional::class.java)
                ?.let { it.propagation != Propagation.NEVER } ?: false
    }

    class JavaxTransactionalAnnotation : DescribedPredicate<JavaAnnotation<*>>("") {
        override fun test(input: JavaAnnotation<*>): Boolean =
            input.toAnnotation(javax.transaction.Transactional::class.java) != null
    }

    private fun JavaMethod.checkOtherImplementationsIfTransactional(
        checkOwner: Boolean
    ): Boolean? {
        return ifArchUnitVerifierPluginContainsOwnerGetAllSubClassesOfOwner(this.owner)
            ?.any { subClass ->
                this.getOverriddenMethodFromSubclass(subClass)
                    ?.let { isTransactional(it, checkOwner) } ?: false
            }
    }

    private fun JavaCodeUnit.getOverriddenMethodFromSubclass(subClass: JavaClass) =
        subClass.methods.find {
            it.name == this.name && HasName.Utils.namesOf(it.rawParameterTypes) == HasName.Utils.namesOf(
                this.rawParameterTypes
            )
        }


    private fun ifArchUnitVerifierPluginContainsOwnerGetAllSubClassesOfOwner(
        owner: JavaClass
    ): MutableSet<JavaClass>? =
        owner.allSubclasses

    fun JavaAccess<*>.isTransactionalFunctionOrInRepositoryClass() =
        isTransactional(this.target)
                || this.target.owner.isAssignableTo(Repository::class.java)

                // unfortunately we cannot extract Kotlin extension functions correctly with ArchUnit, the only way
                // here is to reference to the CrudRepositoryExtensionsKt for this specific case, but it won't work
                // for all other extension functions
                || this.target.owner.isAssignableTo("org.springframework.data.repository.CrudRepositoryExtensionsKt")

}
