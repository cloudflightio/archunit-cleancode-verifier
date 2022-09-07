package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.importer.ClassFileImporter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.transaction.annotation.Transactional

class CloudflightSpringTransactionalRulesTest {

    val rules = SpringTransactionalRules()
    val rulesRepository = SpringTransactionalRepositoryRules()
    val rulesController = SpringTransactionalControllerRules()
    val importerBadCases =
        ClassFileImporter().importPackages(
            "io.cloudflight.cleancode.archunit.rules.spring.txtest.badcase"
        )
    val importerGoodCases =
        ClassFileImporter().importPackages(
            "io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase"
        )

    // TODO split up
    @Test
    fun `transactional is handled correctly`() {

        rules.transactional_methods_should_not_throw_exceptions.check(importerGoodCases)
        rules.transactional_should_never_go_on_class_level.check(importerGoodCases)
        rulesController.controller_methods_should_never_be_transactional.check(importerGoodCases)
        assertThrows<AssertionError> {
            rulesController.controller_methods_should_not_access_more_than_one_transactional_method
                .check(importerGoodCases)
        }

        try {
            rulesRepository.methods_that_are_transactional_should_access_other_transactional_methods_or_repositories
                .check(importerGoodCases)
        } catch (e: AssertionError) {
            // This test cannot deal correctly with Consumers. Therefore only 2 methods should fail!
            assertThat(e.message!!.countOccurrenceOf("callRepoInRunnable()")).isEqualTo(1)
            assertThat(e.message!!.countOccurrenceOf("lambda()")).isEqualTo(1)
            // With Title and the above other two failures
            assertThat(e.message!!.split("\n").size).isEqualTo(3)
        }

        rulesRepository.repository_methods_should_only_be_accessed_by_transactional_methods.check(importerGoodCases)
    }

    private fun String.countOccurrenceOf(pattern: String) =
        this.split(pattern).dropLastWhile { it.isEmpty() }.toTypedArray().size - 1

    @Test
    fun `service calls repository method but is not transactional`() {
        assertThrows<AssertionError> {
            rules.transactional_methods_should_not_throw_exceptions
                .check(importerBadCases)
        }

        assertThrows<AssertionError> {
            rules.transactional_should_never_go_on_class_level
                .check(importerBadCases)
        }

        assertThrows<AssertionError> {
            rulesController.controller_methods_should_never_be_transactional
                .check(importerBadCases)
        }

        assertThrows<AssertionError> {
            rulesController.controller_methods_should_not_access_more_than_one_transactional_method
                .check(importerBadCases)
        }

        assertThrows<AssertionError> {
            rulesRepository.methods_that_are_transactional_should_access_other_transactional_methods_or_repositories
                .check(importerBadCases)
        }

        assertThrows<AssertionError> {
            rulesRepository.repository_methods_should_only_be_accessed_by_transactional_methods
                .check(importerBadCases)
        }
    }

    @Test
    fun handleCheckedExcptionsCorreclty() {
        assertThrows<AssertionError> {
            rules.transactional_methods_should_not_throw_exceptions
                .check(ClassFileImporter().importClasses(ServiceWithHiddenCheckedExceptionAndThrowsClause::class.java))
        }
    }

    open class ServiceWithHiddenCheckedExceptionAndThrowsClause {

        @Transactional
        @Throws(Exception::class)
        open fun callCheckedException() {
            throw Exception("")
        }
    }

    @Test
    @Disabled
    fun handleHiddenCheckedExceptionsCorrectly() {
        assertThrows<AssertionError> {
            rules.transactional_methods_should_not_throw_exceptions
                .check(ClassFileImporter().importClasses(ServiceWithHiddenCheckedException::class.java))
        }
    }

    open class ServiceWithHiddenCheckedException {

        @Transactional
        open fun callCheckedException() {
            throw Exception("")
        }
    }

}
