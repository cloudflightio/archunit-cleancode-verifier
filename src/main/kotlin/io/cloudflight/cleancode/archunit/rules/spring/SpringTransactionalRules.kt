package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.*
import com.tngtech.archunit.core.domain.properties.HasName
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import org.springframework.cache.annotation.Cacheable
import org.springframework.transaction.annotation.Transactional

/**
 * Rules around the usage of Spring's [Transactional]
 */
class SpringTransactionalRules {

    @ArchTest
    val transactional_methods_should_not_throw_exceptions =
        archRuleWithId(
            "spring.tx-do-not-throw-exceptions",
            methods()
                .that(AreTransactional())
                .should(NotThrowExceptions())
        )

    class NotThrowExceptions : ArchCondition<JavaMethod>("not throw exceptions") {
        override fun check(item: JavaMethod, events: ConditionEvents) {
            if (item.throwsClause.any()) {
                events.add(
                    SimpleConditionEvent.violated(
                        item,
                        "$item is @Transactional but throws exceptions"
                    )
                )
            }
        }
    }

    class NotBeTransactionalClass :
        ArchCondition<JavaClass>("not be annotated with @Transactional") {
        override fun check(item: JavaClass, events: ConditionEvents) {
            if (TransactionalHelper.isTransactional(item)) {
                events.add(
                    SimpleConditionEvent.violated(
                        item,
                        "$item should not be annotated with @Transactional"
                    )
                )
            }
        }
    }

    /**
     * Annotations are not inherited in Java. If target-class-proxying strategy is used (i.e. CGLIB is used) class level
     * annotations might get lost unintentionally. Furthermore adding, changing, or removing class level annotations can
     * have unexpected side effects, especially in a shared code base.
     * Behavioral annotations should therefore always be applied on the smallest possible scope, i.e. method level.
     */
    @ArchTest
    val transactional_should_never_go_on_class_level =
        archRuleWithId(
            "spring.tx-no-transactional-on-classlevel",
            classes()
                .should(NotBeTransactionalClass())
        )


    @ArchTest
    val methods_that_are_transactional_should_not_be_cacheable =
        archRuleWithId(
            "spring.tx-transactional-methods-should-not-be-cacheable",
            methods()
                .that(AreTransactional())
                .should(NotBeCacheable())
        )

    private class NotBeCacheable :
        ArchCondition<JavaMethod>("not be annotated with @Cacheable") {
        override fun check(item: JavaMethod, events: ConditionEvents) {
            if (CacheableHelper.isCacheable(item)) {
                events.add(
                    SimpleConditionEvent.violated(
                        item,
                        "$item annotated with @Transactional must not be annotated with @Cacheable"
                    )
                )
            }
        }
    }

    class AreTransactional : DescribedPredicate<JavaMethod>("are @Transactional") {
        override fun test(input: JavaMethod): Boolean {
            return !input.modifiers.contains(JavaModifier.STATIC) && !input.modifiers.contains(JavaModifier.PRIVATE)
                    && TransactionalHelper.isTransactional(input)
        }
    }

    object CacheableHelper {
        @Suppress("ReturnCount")
        fun isCacheable(
            codeUnit: JavaCodeUnit,
            checkOwner: Boolean = false,
            checkSubClasses: Boolean = false
        ): Boolean {
            if (codeUnit.isAnnotatedOrMetaAnnotatedWithCacheable()) return true
            if (checkOwner && codeUnit.isOwnerAnnotatedOrMetaAnnotatedWithCacheable()) return true

            return codeUnit.takeIf { checkSubClasses && it is JavaMethod }
                ?.let { it as JavaMethod }
                ?.checkOtherImplementationsIfCacheable(checkOwner) ?: false
        }

        private fun JavaCodeUnit.isAnnotatedOrMetaAnnotatedWithCacheable() =
            this.isAnnotatedWith(CacheableAnnotation()) || this.isMetaAnnotatedWith(CacheableAnnotation())

        private fun JavaCodeUnit.isOwnerAnnotatedOrMetaAnnotatedWithCacheable() =
            this.owner.isAnnotatedWith(CacheableAnnotation()) || this.owner.isMetaAnnotatedWith(CacheableAnnotation())

        class CacheableAnnotation : DescribedPredicate<JavaAnnotation<*>>("") {
            override fun test(input: JavaAnnotation<*>): Boolean {
                return input.toAnnotation(Cacheable::class.java) != null
            }
        }

        private fun JavaMethod.checkOtherImplementationsIfCacheable(
            checkOwner: Boolean
        ): Boolean? {
            return ifArchUnitVerifierPluginContainsOwnerGetAllSubClassesOfOwner(this.owner)
                ?.any { subClass ->
                    this.getOverriddenMethodFromSubclass(subClass)
                        ?.let { isCacheable(it, checkOwner) } ?: false
                }
        }

        private fun JavaCodeUnit.getOverriddenMethodFromSubclass(subClass: JavaClass) =
            subClass.methods.find {
                it.name == this.name && HasName.Utils.namesOf(it.rawParameterTypes) == HasName.Utils.namesOf(
                    this.rawParameterTypes
                )
            }


        private fun ifArchUnitVerifierPluginContainsOwnerGetAllSubClassesOfOwner(
            owner: JavaClass
        ): MutableSet<JavaClass>? =
            owner.allSubclasses
    }
}
