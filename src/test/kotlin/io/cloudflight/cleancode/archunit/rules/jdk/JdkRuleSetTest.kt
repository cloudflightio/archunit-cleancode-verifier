package io.cloudflight.cleancode.archunit.rules.jdk

import com.tngtech.archunit.core.importer.ClassFileImporter
import io.cloudflight.cleancode.archunit.ArchRuleWithId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.MathContext

class JdkRuleSetTest {

    lateinit var rules: JdkRuleSet

    @BeforeEach
    fun initialize() {
        ArchRuleWithId.FREEZE_ENABLED = false
        rules = JdkRuleSet()
    }

    @Test
    fun doNotThrowGenericExceptions() {
        class MyClassWithAGenericExcpetion {
            fun throwMe() {
                throw Exception()
            }
        }

        val importer = ClassFileImporter().importClasses(MyClassWithAGenericExcpetion::class.java)

        assertThrows<AssertionError> { rules.no_generic_exceptions.check(importer) }
    }

    @Test
    fun doNotThrowGenericExceptionsWithIllegalArgument() {
        class MyClassWithAnIllegalArgumentException {
            fun throwMe() {
                throw IllegalArgumentException()
            }
        }

        val importer = ClassFileImporter().importClasses(MyClassWithAnIllegalArgumentException::class.java)

        assertDoesNotThrow {
            rules.no_generic_exceptions.check(importer)
        }
    }

    @Test
    fun `fail on big decimal constructor with double parameter`() {
        class BigDecimalTestClass {
            private fun toBigDecimal(double: Double): BigDecimal {
                return BigDecimal(double)
            }
        }

        val importer = ClassFileImporter().importClasses(BigDecimalTestClass::class.java)

        assertThrows<AssertionError> { rules.bigdecimal_must_not_use_constructor_with_double_parameter.check(importer) }
    }

    @Test
    fun `fail on big decimal constructor with double and mathContext parameter`() {
        class BigDecimalTestClass {
            private fun toBigDecimal(double: Double): BigDecimal {
                return BigDecimal(double, MathContext.UNLIMITED)
            }
        }

        val importer = ClassFileImporter().importClasses(BigDecimalTestClass::class.java)

        assertThrows<AssertionError> { rules.bigdecimal_must_not_use_constructor_with_double_parameter.check(importer) }
    }
}