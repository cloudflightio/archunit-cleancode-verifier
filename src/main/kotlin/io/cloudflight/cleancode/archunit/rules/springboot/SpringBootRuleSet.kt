package io.cloudflight.cleancode.archunit.rules.springboot

import com.tngtech.archunit.core.domain.JavaConstructor
import com.tngtech.archunit.core.domain.JavaField
import com.tngtech.archunit.core.domain.JavaMember
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvent
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.members
import io.cloudflight.cleancode.archunit.ArchRuleWithId
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.lang.reflect.Parameter

/**
 * Common rules to be applied when using Spring Boot
 */
class SpringBootRuleSet {

    @ArchTest
    val do_not_use_spring_value_annotation =
        archRuleWithId(
            "springboot.context-no-value-annotation",
            members()
                .should(NotUseSpringValueAnnotation())
        )

    private class NotUseSpringValueAnnotation :
        ArchCondition<JavaMember>("use Spring Boot's @ConfigurationProperties instead of @org.springframework.beans.factory.annotation.Value") {

        companion object {
            private val ANNOTATION_CLASS = Value::class.java
        }

        override fun check(item: JavaMember, events: ConditionEvents) {
            try {
                when {
                    item is JavaConstructor || item is JavaMethod -> item.parameters()
                        ?.checkExistenceOfValueAnnotation(item)
                        ?.forEach { events.add(it) }

                    item is JavaField && item.isAnnotatedWith(ANNOTATION_CLASS) -> events.add(
                        SimpleConditionEvent.violated(item, "$item uses @Value")
                    )
                }
            } catch (ex: NoClassDefFoundError) {
                LoggerFactory.getLogger(NotUseSpringValueAnnotation::class.java)
                    .error("Cannot check $item due to $ex")
            }
        }

        private fun JavaMember.parameters() =
            (this as? JavaMethod)?.reflect()?.parameters
                ?: (this as? JavaConstructor)?.reflect()?.parameters


        private fun Array<Parameter>.checkExistenceOfValueAnnotation(item: JavaMember): List<ConditionEvent> =
            this.filter { it.isAnnotationPresent(ANNOTATION_CLASS) }
                .map { SimpleConditionEvent.violated(item, "$item uses @Value") }
    }
}
