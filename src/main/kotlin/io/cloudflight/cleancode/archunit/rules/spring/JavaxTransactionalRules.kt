package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.conditions.ArchConditions
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import io.cloudflight.cleancode.archunit.ArchRuleWithId
import javax.transaction.Transactional

class JavaxTransactionalRules {

    private val USE_JAVAX_TRANSACTIONAL: ArchCondition<JavaClass> =
        ArchConditions.dependOnClassesThat(JavaClass.Predicates.type(Transactional::class.java))
            .`as`("use javax.transaction.Transactional")


    @ArchTest
    val javax_transactional_should_not_be_used =
        ArchRuleWithId.archRuleWithId("spring.tx-no-javax-transactions-transactional-annotations",
        ArchRuleDefinition.noClasses()
        .should(USE_JAVAX_TRANSACTIONAL))
}