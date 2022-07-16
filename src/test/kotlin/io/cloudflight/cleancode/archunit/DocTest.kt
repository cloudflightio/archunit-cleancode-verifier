package io.cloudflight.cleancode.archunit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class DocTest {

    @ParameterizedTest
    @MethodSource("docs")
    fun headersAreCorrect(file: File) {
        val lines = file.readLines().iterator()
        while (lines.hasNext()) {
            val line = lines.next()
            if (line.startsWith(DocParser.START_TOKEN)) {
                val header = lines.next()
                assertThat(header).startsWith("###")
                val ruleDescription = lines.next()
                val ruleId = file.name.substringBefore(".") + "." + line.toRuleId()
                assertThat(ruleDescription).isEqualTo("<sup>`Rule-ID: $ruleId`</sup>")
            }
        }
    }

    companion object {
        @JvmStatic
        fun docs() = File("rules").listFiles()
    }
}