package io.cloudflight.cleancode.archunit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArchRuleWithIdTest {

    @Test
    fun idToUrl() {
        val idToUrl = ArchRuleWithId.idToUrl("jpa.fix-stuff")
        assertThat(idToUrl).endsWith("/rules/jpa.md#user-content-fix-stuff")
    }
}