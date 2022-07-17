package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.javaField

class DocumentationTest {

    @ParameterizedTest
    @MethodSource("classes")
    fun `all rules are explained in the documentation`(clazz: KClass<*>) {
        // we need to deactivate freezing here, otherwise we can't access the ArchRuleWithId
        // anymore, which will be a private delegate of the FreezingArchRule then
        ArchRuleWithId.FREEZE_ENABLED = false
        val file = clazz.simpleName!!.lowercase().removeSuffix("ruleset")
        val ids = extractIds(clazz, file)
        val headers = DocParser("rules/${file}.md").getHeaders().map { "${file}.${it}" }
        assertThat(ids).containsExactlyInAnyOrderElementsOf(headers)
        ArchRuleWithId.FREEZE_ENABLED = true
    }

    private fun extractIds(clazz: KClass<*>, file: String): List<String> {
        val ids = getArchTests(clazz)
            .map {
                val test = it.call(clazz.createInstance())
                if (test is ArchRuleWithId) {
                    listOf(test.id.removeSuffix("$file-"))
                } else if (test is ArchTests) {
                    extractIds(test.definitionLocation.kotlin, file)
                } else {
                    emptyList()
                }
            }.flatten()
        return ids
    }

    companion object {
        @JvmStatic
        fun classes() = getArchTests(CleanCodeRuleSets::class).map {
            (it.call(CleanCodeRuleSets::class.createInstance()) as ArchTests).definitionLocation.kotlin
        }

        /**
         * this is q quite simplified version of the [com.tngtech.archunit.junit.internal.ArchUnitTestEngine]
         * in order to discover [ArchTest]s inside a class. As long as we're just
         * using members, that should be fine
         */
        private fun getArchTests(clazz: KClass<*>): List<KCallable<*>> {
            return clazz.declaredMembers
                .filterIsInstance(KProperty::class.java)
                .filter {
                    it.javaField!!.isAnnotationPresent(ArchTest::class.java)
                }
        }
    }
}


