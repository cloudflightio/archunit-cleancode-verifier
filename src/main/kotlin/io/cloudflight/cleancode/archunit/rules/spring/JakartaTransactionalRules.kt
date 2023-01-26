package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.conditions.ArchConditions
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import io.cloudflight.cleancode.archunit.ArchRuleWithId
import jakarta.transaction.Transactional

class JakartaTransactionalRules {

    private val USE_JAKARTA_TRANSACTIONAL: ArchCondition<JavaClass> =
        ArchConditions.dependOnClassesThat(JavaClass.Predicates.type(Transactional::class.java))
            .`as`("use jakarta.transaction.Transactional")


    @ArchTest
    val jakarta_transactional_should_not_be_used =
        ArchRuleWithId.archRuleWithId("spring.tx-no-jakarta-transactions-transactional-annotations",
        ArchRuleDefinition.noClasses()
        .should(USE_JAKARTA_TRANSACTIONAL))
}