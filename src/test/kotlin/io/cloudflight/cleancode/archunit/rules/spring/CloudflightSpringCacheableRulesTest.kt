package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CloudflightSpringCacheableRulesTest {

    val rules = SpringCacheableRules()
    private val importerBadCases: JavaClasses =
        ClassFileImporter().importPackages(
            "io.cloudflight.cleancode.archunit.rules.spring.txtest.badcase"
        )
    private val importerGoodCases: JavaClasses =
        ClassFileImporter().importPackages(
            "io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase"
        )

    @Test
    fun `Cacheables are handled correctly`() {
        rules.cacheable_methods_should_not_be_called_from_functions_in_same_class.check(importerGoodCases)
    }

    @Test
    fun `Cacheable functions are called internally which should assert an issue`() {
        assertThrows<AssertionError> {
            rules.cacheable_methods_should_not_be_called_from_functions_in_same_class
                .check(importerBadCases)
        }
    }
}
