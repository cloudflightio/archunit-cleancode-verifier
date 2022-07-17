package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaAnnotation
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvent
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable

/**
 * Rules around the usage of Spring's [Cacheable]
 */
class SpringCacheableRules {

    @ArchTest
    val cacheable_methods_should_not_be_called_from_functions_in_same_class =
        archRuleWithId(
            "spring.context-cacheable-annotated-functions-should-not-be-called-from-function-in-same-class",
            classes()
                .should(NotBeCalledFromAnotherFunctionInSameClass())
        )


    private class NotBeCalledFromAnotherFunctionInSameClass :
        ArchCondition<JavaClass>("not be called from another function inside same class") {
        override fun check(item: JavaClass, events: ConditionEvents) {
            item.methods
                .mapNotNull { it.getFunctionCallingCacheableFunctionInSameClass(item) }
                .forEach { events.add(it) }
        }

        private fun JavaMethod.getFunctionCallingCacheableFunctionInSameClass(item: JavaClass): ConditionEvent? =
            this.methodCallsFromSelf
                .find { it.target.isAnnotatedWith(CacheAnnotation()) && it.target.owner == item }
                ?.let {
                    SimpleConditionEvent.violated(
                        item,
                        "${this.name} is calling a Cacheable function (${it.target.fullName}) inside same Class"
                    )
                }
    }


    class CacheAnnotation : DescribedPredicate<JavaAnnotation<*>>("") {
        override fun test(input: JavaAnnotation<*>): Boolean =
            (input.toAnnotation(Cacheable::class.java)
                ?: input.toAnnotation(CachePut::class.java)
                ?: input.toAnnotation(CacheEvict::class.java)) != null
    }
}
