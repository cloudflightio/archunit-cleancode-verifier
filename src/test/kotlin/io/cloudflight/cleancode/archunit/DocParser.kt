package io.cloudflight.cleancode.archunit

import java.io.File

class DocParser(private val docPath: String) {

    fun getHeaders() =
        File(docPath)
            .useLines { it.toList() }
            .filter { it.startsWith(START_TOKEN) }
            .map { it.toRuleId() }

    companion object {
        const val START_TOKEN ="<a id=\""
    }
}

fun String.toRuleId():String {
    return this.removePrefix(DocParser.START_TOKEN).substringBefore("\"")
}
