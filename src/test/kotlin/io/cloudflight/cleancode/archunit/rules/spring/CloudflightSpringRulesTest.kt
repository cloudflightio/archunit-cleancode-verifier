package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.importer.ClassFileImporter
import io.cloudflight.cleancode.archunit.rules.spring.txtest.badcase.FinalService
import io.cloudflight.cleancode.archunit.rules.springboot.SpringBootRuleSet
import org.assertj.core.api.Fail.fail
import org.junit.jupiter.api.Test

class CloudflightSpringRulesTest {

    val rules = SpringBootRuleSet()
    val springRules = SpringRuleSet()

    @Test
    fun finalComponents() {
        val importer = ClassFileImporter().importClasses(FinalService::class.java)

        try {
            springRules.do_not_make_components_final.check(importer)
            fail<String>("expecting assertion error")
        } catch (ex: AssertionError) {
            // ok
        }
    }

    @Test
    fun springValueOnConstructor() {
        val importer = ClassFileImporter().importClasses(ClassWithValue::class.java)

        try {
            rules.do_not_use_spring_value_annotation.check(importer)
            fail<String>("expecting assertion error")
        } catch (ex: AssertionError) {
            // ok
        }
    }

    @Test
    fun springValueOnMethods() {
        val importer = ClassFileImporter().importClasses(ClassWithValueOnMethod::class.java)

        try {
            rules.do_not_use_spring_value_annotation.check(importer)
            fail<String>("expecting assertion error")
        } catch (ex: AssertionError) {
            // ok
        }
    }

    @Test
    fun springValueOnFields() {
        val importer = ClassFileImporter().importClasses(ClassWithValueOnField::class.java)

        try {
            rules.do_not_use_spring_value_annotation.check(importer)
            fail<String>("expecting assertion error")
        } catch (ex: AssertionError) {
            // ok
        }
    }
}
