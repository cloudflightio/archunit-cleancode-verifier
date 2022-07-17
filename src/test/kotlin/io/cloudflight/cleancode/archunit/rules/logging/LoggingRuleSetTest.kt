package io.cloudflight.cleancode.archunit.rules.logging

import com.tngtech.archunit.core.importer.ClassFileImporter
import io.cloudflight.cleancode.archunit.ArchRuleWithId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LoggingRuleSetTest {

    lateinit var rules: LoggingRuleSet

    @BeforeEach
    fun initialize() {
        ArchRuleWithId.FREEZE_ENABLED = false
        rules = LoggingRuleSet()
    }

    @Test
    fun failOnPublicLogger1() {
        failOnPublicLogger(1)
    }

    @Test
    fun failOnPublicLogger2() {
        failOnPublicLogger(2)
    }

    @Test
    fun failOnPublicLogger3() {
        failOnPublicLogger(3)
    }

    @Test
    fun failOnPublicLogger4() {
        failOnPublicLogger(4)
    }

    @Test
    fun failOnPublicLogger5() {
        failOnPublicLogger(5)
    }

    @Test
    fun doNotFailOnPrivateLogger1() {
        checkLoggerRules("private" + 1)
    }

    @Test
    fun doNotFailOnPrivateLogger2() {
        checkLoggerRules("private" + 2)
    }

    private fun failOnPublicLogger(usecase: Int) {
        assertThrows<AssertionError> { checkLoggerRules("public$usecase") }
    }

    private fun checkLoggerRules(subPackage: String) {
        val classes =
            ClassFileImporter().importPackages("io.cloudflight.cleancode.archunit.rules.logging." + subPackage)
        rules.loggers_should_be_private_static_final.check(classes)
        rules.loggers_must_not_be_exposed_as_public_methods.check(classes)
        rules.loggers_must_implement_klogging.check(classes)
    }
}
