package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.freeze.ViolationStore
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.*

internal object IdBasedViolationStore : ViolationStore {

    private var storeCreationAllowed: Boolean = false
    private var storeUpdateAllowed: Boolean = false
    private lateinit var storeFolder: Path

    override fun initialize(properties: Properties) {
        storeCreationAllowed = properties.getProperty("default.allowStoreCreation", false.toString()).toBooleanStrict()
        storeUpdateAllowed = properties.getProperty("default.allowStoreUpdate", true.toString()).toBooleanStrict()
        storeFolder = Paths.get(properties.getProperty("default.path", "archunit_store")).also {
            it.createDirectories()
        }
    }

    override fun contains(rule: ArchRule): Boolean {
        if (rule is ArchRuleWithId) {
            return getViolationsFile(rule).exists()
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
            return getViolationsFile(rule).readLines()
        } else {
            throw IllegalArgumentException("Expected ArchRuleWithId")
        }
    }

    private fun getViolationsFile(rule: ArchRuleWithId): Path {
        return storeFolder.resolve(rule.id)
    }
}
