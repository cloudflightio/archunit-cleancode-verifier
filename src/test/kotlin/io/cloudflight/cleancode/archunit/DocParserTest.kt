package io.cloudflight.cleancode.archunit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DocParserTest {

    @Test
    fun extractHeaders() {
        val parser = DocParser("rules/jpa.md")
        val headers = parser.getHeaders()
        assertThat(headers)
            .isNotEmpty
            .contains("entities-no-column-names")
    }
}