package io.cloudflight.cleancode.archunit.kotlin

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import io.cloudflight.archunit.kotlin.KotlinEntityNullableCondition
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import org.slf4j.LoggerFactory
import javax.persistence.Embeddable
import javax.persistence.Entity

class KotlinJpaRuleSet {

    @ArchTest
    val nullable_flag_of_kotlin_needs_to_match_jpa_specification =
        archRuleWithId(
            "kotlin.jpa.nullable-flag-of-kotlin-needs-to-match-jpa-specification",
            classes()
                .that(AreKotlinClasses())
                .and(AreJpaEntities())
                .should(KotlinEntityNullableCondition())
        )


    class AreJpaEntities : DescribedPredicate<JavaClass>("are JPA Entity classes") {
        override fun test(input: JavaClass): Boolean {
            return input.isAnnotatedWith(Entity::class.java) || input.isAnnotatedWith(Embeddable::class.java)
        }
    }

    class AreKotlinClasses : DescribedPredicate<JavaClass>("are kotlin classes") {
        override fun test(input: JavaClass): Boolean {
            return try {
                input.reflect().isKotlinClass()
            } catch (err: NoClassDefFoundError) {
                LOG.error("Cannot detect if $input is a KotlinClass due to $err. Assuming it's not a Kotlin class")
                false
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KotlinJpaRuleSet::class.java)
    }
}
