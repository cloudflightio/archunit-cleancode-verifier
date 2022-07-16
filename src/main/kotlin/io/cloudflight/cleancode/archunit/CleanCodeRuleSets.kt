package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchTests
import io.cloudflight.cleancode.archunit.rules.jpa.JpaRuleSet
import io.cloudflight.cleancode.archunit.rules.jdk.JdkRuleSet
import io.cloudflight.cleancode.archunit.rules.logging.LoggingRuleSet

class CleanCodeRuleSets {

    @ArchTest
    val jpa = ArchTests.`in`(JpaRuleSet::class.java)

    @ArchTest
    val logging = ArchTests.`in`(LoggingRuleSet::class.java)

    @ArchTest
    val jdk = ArchTests.`in`(JdkRuleSet::class.java)

}
