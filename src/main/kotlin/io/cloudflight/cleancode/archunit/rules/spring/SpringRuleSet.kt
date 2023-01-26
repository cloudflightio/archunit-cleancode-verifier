package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaAnnotation
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import io.cloudflight.cleancode.archunit.utils.ruleSetOf
import org.springframework.stereotype.Component

/**
 * Common rules to be applied when using the Spring Framework
 */
class SpringRuleSet {

    @ArchTest
    val do_not_make_components_final =
        archRuleWithId(
            "spring.context-no-final-components",
            classes()
                .that(AreComponents())
                .should(NotBeFinal())
        )

    private class AreComponents : DescribedPredicate<JavaClass>("are @Component") {
        override fun test(input: JavaClass): Boolean {
            return input.isAnnotatedWith(ComponentAnnotation())
        }
    }

    private class ComponentAnnotation : DescribedPredicate<JavaAnnotation<*>>("") {
        override fun test(input: JavaAnnotation<*>): Boolean {
            return input.toAnnotation(Component::class.java) != null
        }
    }

    private class NotBeFinal :
        ArchCondition<JavaClass>("must not be final") {
        override fun check(item: JavaClass, events: ConditionEvents) {
            if (item.modifiers.contains(JavaModifier.FINAL)) {
                events.add(SimpleConditionEvent.violated(item, "ADR-0015: $item must not be final"))
            }
        }
    }

    @ArchTest
    val jakartaTransactional = ruleSetOf(
        JakartaTransactionalRules::class.java,
        requiredClasses = arrayOf(
            "jakarta.transaction.Transactional"
        )
    )

    @ArchTest
    val springTransactional = ruleSetOf(
        SpringTransactionalRules::class.java,
        requiredClasses = arrayOf(
            "org.springframework.transaction.annotation.Transactional"
        )
    )

    @ArchTest
    val springTransactionalController = ruleSetOf(
        SpringTransactionalControllerRules::class.java,
        requiredClasses = arrayOf(
            "org.springframework.transaction.annotation.Transactional",
            "org.springframework.web.bind.annotation.RestController"
        )
    )

    @ArchTest
    val springTransactionalRepositoryRules = ruleSetOf(
        SpringTransactionalRepositoryRules::class.java,
        requiredClasses = arrayOf(
            "org.springframework.transaction.annotation.Transactional",
            "org.springframework.data.jpa.repository.JpaRepository"
        )
    )

    @ArchTest
    val springWeb = ruleSetOf(
        SpringWebRules::class.java,
        requiredClasses = arrayOf(
            "org.springframework.web.bind.annotation.RequestMapping"
        )
    )

    @ArchTest
    val springCacheableRules = ruleSetOf(
        SpringCacheableRules::class.java,
        requiredClasses = arrayOf(
            "org.springframework.cache.annotation.CachePut"
        )
    )

}
