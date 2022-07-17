package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController

/**
 * Rules around the usage of Spring's [Transactional] in connection with [RestController]
 */
class SpringTransactionalControllerRules {

    /**
     * If controller methods are annotated with @Transactional, depending of the order of the advices in the chain even
     * databinding, input validation, security checks, etc. are executed within a database transaction. Furthermore not
     * all execution paths always end up in database access.
     * Having controller methods annotated with @Transactional exposes database resources to the outside, makes it subject
     * to denial of service attacks and is a common cause for spontaneous pool exhaustions.
     */
    @ArchTest
    val controller_methods_should_never_be_transactional =
        archRuleWithId(
            "spring.tx-controller-methods-should-not-be-transactional",
            methods()
                .that(ArePublicAndDeclaredInSpringControllers())
                .should(NotBeTransactional())
        )

    /**
     *  Transactions shall be scoped in the service layer and only be opened for validated and authenticated requests.
     *  In order to achieve that it is vital that no more than one transactional service method is called from within
     *  a controller.
     */
    @ArchTest
    val controller_methods_should_not_access_more_than_one_transactional_method =
        archRuleWithId(
            "spring.tx-controller-methods-should-not-access-more-than-one-transactional-method",
            methods()
                .that(ArePublicAndDeclaredInSpringControllers())
                .should(NotAccessMoreThanOneTransactionalMethod())
        )

    private class ArePublicAndDeclaredInSpringControllers :
        DescribedPredicate<JavaMethod>("are public and declared in spring controllers") {
        override fun test(input: JavaMethod): Boolean {
            return input.modifiers.contains(JavaModifier.PUBLIC) &&
                    (input.owner.isAnnotatedWith(RestController::class.java) || input.owner.isMetaAnnotatedWith(
                        RestController::class.java
                    ) ||
                            input.owner.isAnnotatedWith(Controller::class.java) || input.owner.isMetaAnnotatedWith(
                        Controller::class.java
                    ))
        }
    }

    private class NotAccessMoreThanOneTransactionalMethod :
        ArchCondition<JavaMethod>("not access more than one transactional method") {
        override fun check(item: JavaMethod, events: ConditionEvents) {
            val transactionCount = JavaMethodHelper.collectTransitiveCalls(item, true)
                .count {
                    TransactionalHelper.isTransactional(
                        it,
                        checkOwner = true,
                        checkSubClasses = true
                    )
                }

            if (transactionCount > 1) {
                events.add(
                    SimpleConditionEvent.violated(
                        item,
                        "$item calls $transactionCount (more than one) other @Transactional methods"
                    )
                )
            }
        }
    }
}
