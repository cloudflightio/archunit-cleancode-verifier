package io.cloudflight.cleancode.archunit.rules.jdk

import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.GeneralCodingRules
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import java.math.BigDecimal
import java.math.MathContext

class JdkRuleSet {

    @ArchTest
    val no_generic_exceptions =
        archRuleWithId(
            "jdk.no-generic-exceptions",
            noClasses().should(GeneralCodingRules.THROW_GENERIC_EXCEPTIONS)
        )

    @ArchTest
    val no_jodatime = archRuleWithId("jdk.no-jodatime", GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME)

    @ArchTest
    val bigdecimal_must_not_use_constructor_with_double_parameter =
        archRuleWithId(
            "jdk.bigdecimal-do-not-call-constructor-with-double-parameter",
            noClasses()
                .should().callConstructor(BigDecimal::class.java, Double::class.java)
                .orShould().callConstructor(BigDecimal::class.java, Double::class.java, MathContext::class.java)
        )

}