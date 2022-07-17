package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

class SpringWebRulesTest {

    val rules = SpringWebRules()

    @Test
    fun `RequestMapping must not be put on interfaces`() {
        assertThrows<AssertionError> {
            rules.requestmapping_is_not_allowed_on_top_level
                .check(ClassFileImporter().importClasses(MyBadApi::class.java))
        }
    }

    @Test
    fun `RequestMapping can be put on interface methods`() {
        rules.requestmapping_is_not_allowed_on_top_level
            .check(ClassFileImporter().importClasses(MyGoodApi::class.java))
    }

    @Test
    fun `RequestMapping can be put on classes`() {
        rules.requestmapping_is_not_allowed_on_top_level
            .check(ClassFileImporter().importClasses(MyController::class.java))
    }

    @RequestMapping("/test")
    interface MyBadApi {

        @GetMapping("/foo")
        fun myMethod()
    }

    interface MyGoodApi {

        @GetMapping("/test/foo")
        fun myMethod()
    }

    @RequestMapping("/test")
    class MyController {

    }

}
