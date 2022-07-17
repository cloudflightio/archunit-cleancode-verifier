package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.junit.ArchTest
import io.cloudflight.cleancode.archunit.rules.jdk.JdkRuleSet
import io.cloudflight.cleancode.archunit.rules.jpa.JpaRuleSet
import io.cloudflight.cleancode.archunit.rules.logging.LoggingRuleSet
import io.cloudflight.cleancode.archunit.rules.spring.SpringRuleSet
import io.cloudflight.cleancode.archunit.rules.springboot.SpringBootRuleSet
import io.cloudflight.cleancode.archunit.utils.ruleSetOf

class CleanCodeRuleSets {

    @ArchTest
    val jpa = ruleSetOf(
        JpaRuleSet::class.java,
        requiredClasses = arrayOf(
            "javax.persistence.Entity",
            "javax.validation.constraints.NotNull"
        )
    )

    @ArchTest
    val logging = ruleSetOf(LoggingRuleSet::class.java)

    @ArchTest
    val jdk = ruleSetOf(JdkRuleSet::class.java)

    @ArchTest
    val spring = ruleSetOf(
        SpringRuleSet::class.java,
        requiredClasses = arrayOf(
            "org.springframework.stereotype.Component"
        )
    )

    @ArchTest
    val springBoot = ruleSetOf(
        SpringBootRuleSet::class.java,
        requiredClasses = arrayOf(
            "org.springframework.boot.context.properties.ConfigurationProperties"
        )
    )

}
