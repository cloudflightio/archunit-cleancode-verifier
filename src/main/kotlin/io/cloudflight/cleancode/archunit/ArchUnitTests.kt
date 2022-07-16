package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchTests
import io.cloudflight.cleancode.archunit.jpa.JpaEntityRuleSet
import io.cloudflight.cleancode.archunit.kotlin.KotlinJpaRuleSet

class ArchUnitTests {

    @ArchTest
    val kotlin = ArchTests.`in`(KotlinJpaRuleSet::class.java)

    @ArchTest
    val jpa = ArchTests.`in`(JpaEntityRuleSet::class.java)

}
