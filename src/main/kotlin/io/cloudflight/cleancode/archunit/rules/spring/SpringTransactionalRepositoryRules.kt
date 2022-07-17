package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvent
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import io.cloudflight.cleancode.archunit.rules.spring.TransactionalHelper.isTransactionalFunctionOrInRepositoryClass
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Rules around the usage of Spring's [Transactional] in connection with [Repository]
 */
class SpringTransactionalRepositoryRules {

    @ArchTest
    val repository_methods_should_only_be_accessed_by_transactional_methods =
        archRuleWithId(
            "spring.tx-repository-only-from-transactional-methods",
            methods()
                .that(AccessJpaRepositoryMethods())
                .should(BeTransactional())
                .orShould(BeNonPublic())
                .orShould(BeMemberOfInnerOrAnonymousClass())
                .orShould(BeDefaultMethod())
        )


    class BeNonPublic : ArchCondition<JavaMethod>("be non-public and only be accessed by transactional methods") {
        override fun check(item: JavaMethod, events: ConditionEvents) {
            if (item.modifiers.contains(JavaModifier.PUBLIC)) {
                events.add(
                    SimpleConditionEvent.violated(
                        item,
                        "$item should be @Transactional as it is a public method"
                    )
                )
            }
        }
    }

    class AccessJpaRepositoryMethods : DescribedPredicate<JavaMethod>("access repository methods") {
        override fun test(input: JavaMethod): Boolean =
            input.accessesFromSelf.any { it.target.owner.isAssignableTo(JpaRepository::class.java) }
    }

    class BeTransactional :
        ArchCondition<JavaMethod>("be annotated with @Transactional") {
        override fun check(item: JavaMethod, events: ConditionEvents) {
            if (!TransactionalHelper.isTransactional(item)) {
                events.add(
                    SimpleConditionEvent.violated(
                        item,
                        "$item should be annotated with @Transactional as it accesses a repository method"
                    )
                )
            }
        }
    }

    @ArchTest
    val methods_that_are_transactional_should_access_other_transactional_methods_or_repositories =
        archRuleWithId(
            "spring.tx-transactional-methods-should-access-other-transactional-methods-or-repositories",
            methods()
                .that(AreTransactional())
                .should(CallOtherTransactionalOrRepositoryMethods())
        )

    private class CallOtherTransactionalOrRepositoryMethods :
        ArchCondition<JavaMethod>("call other methods that are @Transactional or repository methods") {
        override fun check(item: JavaMethod, events: ConditionEvents) {
            listOf(item).plus(JavaMethodHelper.collectTransitiveCalls(item))
                .flatMap { it.accessesFromSelf }
                .find { javaMethodCall -> javaMethodCall.isTransactionalFunctionOrInRepositoryClass() }
                ?: events.add(item.createEvent())
        }


        private fun JavaMethod.createEvent(): ConditionEvent = SimpleConditionEvent.violated(
            this,
            "$this calls no other @Transactional method and also no repository method"
        )
    }

}
