package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.freeze.ViolationStore
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.*

/**
 * Our [ArchRuleWithId] adds a URL to GitHub which contains the current version of this library
 * (i.e. 0.0.2) to the [ArchRule.because] declaration and that URL changes on every release of this
 * library, therefore also the unique identifier of the rule changes with every release and the
 * default [ViolationStore] would blow up over time. Inspired by the discussion on
 * [this issue](https://github.com/TNG/ArchUnit/issues/751) we created this alternative implementation to the
 * `TextFileBasedViolationStore`
 *
 * The key differences are:
 *
 * 1. we are using [ArchRuleWithId.id] as identifier for rules and take that as the file name to
 * store violations per rule
 * 2. There is no need to set `freeze.store.default.allowStoreCreation` to `true`, it is always `true`
 * 3. Instead of the `stored.rules` file we are using a `knownCleanCodeRules.txt` with a slightly different format
 * 4. we don't store empty violation files and in turn delete them when they are empty
 *
 * Just like in the default implementation,
 *
 * * you can set the property
 * `freeze.store.default.allowStoreUpdate` to `false` in order to avoid that the store is being
 * updated. It makes sense to set that property especially in CI environments
 * * the folder for the violation files comes from `freeze.store.default.allowStoreUpdate`, the
 * default being `archunit_store`
 */
internal object IdBasedViolationStore : ViolationStore {

    private lateinit var knownRules: MutableSet<String>
    private var storeUpdateAllowed: Boolean = false
    private lateinit var storeFolder: Path
    private lateinit var knownRulesFile: Path

    internal const val KNOWN_CLEAN_CODE_RULES_FILE = "knownCleanCodeRules.txt"

    /**
     * A set of all rules that were once here in this library but have been removed over time.
     * We clean up existing stores based on that list.
     */
    private val REMOVED_RULES = setOf(
        "spring.tx-transactional-methods-should-not-be-cacheable"
    )

    override fun initialize(properties: Properties) {
        storeUpdateAllowed = properties.getProperty("default.allowStoreUpdate", true.toString()).toBooleanStrict()
        storeFolder = Paths.get(properties.getProperty("default.path", "archunit_store")).also {
            it.createDirectories()
        }
        knownRulesFile = storeFolder.resolve(KNOWN_CLEAN_CODE_RULES_FILE)
        if (knownRulesFile.exists()) {
            val fileContent = knownRulesFile.readLines().toMutableList()
            var removedRules = false
            REMOVED_RULES.forEach { removedRule ->
                if (fileContent.contains(removedRule)) {
                    fileContent.remove(removedRule)
                    getViolationsFile(removedRule).deleteIfExists()
                    removedRules = true
                }
            }
            if (removedRules) {
                knownRulesFile.writeLines(fileContent, Charsets.UTF_8)
            }
            knownRules = fileContent.toMutableSet()
        } else {
            knownRulesFile.createFile()
            knownRules = mutableSetOf()
        }
    }

    override fun contains(rule: ArchRule): Boolean {
        if (rule is ArchRuleWithId) {
            return knownRules.contains(rule.id)
        } else {
            throw IllegalArgumentException("Expected ArchRuleWithId")
        }
    }

    override fun save(rule: ArchRule, violations: List<String>) {
        if (!storeUpdateAllowed) {
            throw IllegalArgumentException(
                "Updating frozen violations is disabled"
            )
        }
        if (rule is ArchRuleWithId) {
            if (!knownRules.contains(rule.id)) {
                knownRules.add(rule.id)
                knownRulesFile.appendLines(listOf(rule.id))
            }
            val violationsFile = getViolationsFile(rule)
            if (violations.isNotEmpty()) {
                violationsFile.writeLines(
                    violations,
                    Charsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            } else {
                violationsFile.deleteIfExists()
            }
        } else {
            throw IllegalArgumentException("Expected ArchRuleWithId")
        }
    }

    override fun getViolations(rule: ArchRule): List<String> {
        if (rule is ArchRuleWithId) {
            val violationsFile = getViolationsFile(rule)
            if (violationsFile.exists()) {
                return violationsFile.readLines()
            } else {
                return emptyList()
            }
        } else {
            throw IllegalArgumentException("Expected ArchRuleWithId")
        }
    }

    private fun getViolationsFile(rule: ArchRuleWithId): Path {
        return getViolationsFile(rule.id)
    }
    private fun getViolationsFile(ruleId: String): Path {
        return storeFolder.resolve(ruleId)
    }
}
