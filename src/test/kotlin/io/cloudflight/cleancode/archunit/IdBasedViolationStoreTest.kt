package io.cloudflight.cleancode.archunit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*
import kotlin.io.path.appendLines

class IdBasedViolationStoreTest {

    @Test
    fun cleanupRemovedRules(@TempDir directory: File) {
        val file = File(directory, IdBasedViolationStore.KNOWN_CLEAN_CODE_RULES_FILE)
        file.createNewFile()
        file.toPath().appendLines(
            listOf(
                "spring.rule1",
                "spring.tx-transactional-methods-should-not-be-cacheable",
                "spring.rule2",
            )
        )

        val violationsFile = File(directory, "spring.tx-transactional-methods-should-not-be-cacheable")
        violationsFile.createNewFile()

        IdBasedViolationStore.initialize(Properties().apply {
            this["default.path"] = directory.absolutePath
        })

        assertThat(file.readLines())
            .`as`("removed rules have to be removed from ${IdBasedViolationStore.KNOWN_CLEAN_CODE_RULES_FILE}")
            .containsExactly(
                "spring.rule1",
                "spring.rule2"
            )

        assertThat(violationsFile)
            .`as`("removed rule violations have to be removed")
            .doesNotExist()
    }
}