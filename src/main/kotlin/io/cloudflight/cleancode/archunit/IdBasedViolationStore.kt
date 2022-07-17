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
 * 2. the method [ViolationStore.contains] returns `true` for all [ArchRuleWithId] instances. That will
 * also help us later if we add additional rules to this library. In case they find violations,
 * those violations will fail and not automatically and silently end up in the store
 * 3. therefore, there is no need to set `freeze.store.default.allowStoreCreation` to `true`
 * 4. no `stored.rules` file is being created
 * 5. we don't store empty violation files and in turn delete them when they are empty
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

    private var storeUpdateAllowed: Boolean = false
    private lateinit var storeFolder: Path

    override fun initialize(properties: Properties) {
        storeUpdateAllowed = properties.getProperty("default.allowStoreUpdate", true.toString()).toBooleanStrict()
        storeFolder = Paths.get(properties.getProperty("default.path", "archunit_store")).also {
            it.createDirectories()
        }
    }

    override fun contains(rule: ArchRule): Boolean {
        if (rule is ArchRuleWithId) {
            // We don't store empty violation files and compared to the TextFileBasedViolationStore
            // we also don't a keep stored.rules file, so we consider that every ArchRuleWithId
            // is in the store.
            // That will also help us later if we add additional rules to this library. In case
            // they find violations, those violations will fail and not automatically and
            // silently end up in the store
            return true
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
            val violationsFile = getViolationsFile(rule)
            if (violations.isNotEmpty()) {
                violationsFile.writeLines(
                    violations,
                    Charsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            } else {
                if (violationsFile.exists()) {
                    violationsFile.deleteExisting()
                }
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
        return storeFolder.resolve(rule.id)
    }
}
