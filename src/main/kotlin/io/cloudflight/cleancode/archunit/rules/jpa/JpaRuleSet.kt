package io.cloudflight.cleancode.archunit.rules.jpa

import com.tngtech.archunit.core.domain.JavaField
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import io.cloudflight.cleancode.archunit.utils.AreKotlinClasses
import javax.persistence.Column
import javax.persistence.ElementCollection
import kotlin.reflect.KProperty1

class JpaRuleSet {

    @ArchTest
    val do_not_use_column_nullable =
        archRuleWithId(
            "jpa.entities-not-null-instead-of-nullable",
            fields().that()
                .areNotAnnotatedWith(ElementCollection::class.java)
                .should(NotUseTheAnnotationField(Column::nullable))
        )

    @ArchTest
    val do_not_use_column_length =
        archRuleWithId(
            "jpa.entities-size-instead-of-length",
            fields().should(NotUseTheAnnotationField(Column::length))
        )

    @ArchTest
    val do_not_use_column_names =
        archRuleWithId(
            "jpa.entities-no-column-names",
            fields().should(NotUseTheAnnotationField(Column::name))
        )

    @ArchTest
    val nullable_flag_of_kotlin_needs_to_match_jpa_specification =
        archRuleWithId(
            "jpa.nullable-flag-of-kotlin-needs-to-match-jpa-specification",
            ArchRuleDefinition.classes()
                .that(AreKotlinClasses())
                .and(AreJpaEntities())
                .should(KotlinEntityNullableCondition())
        )


}

private class NotUseTheAnnotationField(private val property: KProperty1<Column, Any>) :
    ArchCondition<JavaField>("not use the annotation") {
    override fun check(item: JavaField, events: ConditionEvents) {
        item.takeIf { it.isAnnotatedWith(Column::class.java) }
            ?.getAnnotationOfType(Column::class.java)
            ?.takeIf { it.columnAnnotationHasNotDefaultParameterOfProperty() }
            ?.apply {
                events.add(
                    SimpleConditionEvent.violated(
                        item,
                        "${item.fullName}: Do not use the field Column.${property.name}"
                    )
                )
            }
    }

    private fun Column.columnAnnotationHasNotDefaultParameterOfProperty() =
        property.get(this) != Column::class.java.getDeclaredMethod(property.name).defaultValue

}
