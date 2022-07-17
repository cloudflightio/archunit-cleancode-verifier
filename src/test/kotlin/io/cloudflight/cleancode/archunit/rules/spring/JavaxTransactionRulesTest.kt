package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JavaxTransactionRulesTest {

    val rules = JavaxTransactionalRules()
    val importerBadCases =
        ClassFileImporter().importPackages(
            "io.cloudflight.cleancode.archunit.rules.spring.txtest.badcase"
        )


    @Test
    fun `service calls repository method but is not transactional`() {
        assertThrows<AssertionError> {
            rules.javax_transactional_should_not_be_used
                .check(importerBadCases)
        }
    }
}