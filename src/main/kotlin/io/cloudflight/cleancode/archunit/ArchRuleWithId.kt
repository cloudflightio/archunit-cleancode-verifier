package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.ArchConfiguration
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.freeze.FreezingArchRule

class ArchRuleWithId(val id: String, rule: ArchRule) :
    ArchRule by rule.because("$URL#user-content-$id") {

    companion object {
        fun archRuleWithId(id: String, delegate: ArchRule): ArchRule {
            return if (FREEZE_ENABLED) {
                FreezingArchRule.freeze(ArchRuleWithId(id, delegate)).persistIn(IdBasedViolationStore)
            } else {
                ArchRuleWithId(id, delegate)
            }
        }

        private val VERSION = "v${ArchRuleWithId::class.java.`package`.implementationVersion}"
        private val URL = "https://github.com/cloudflightio/archunit-cleancode-verifier/blob/$VERSION/rules.md"
        private val FREEZE_ENABLED = true.toString() == ArchConfiguration.get().getPropertyOrDefault("cleancode.activate-freeze", false.toString())
    }
}
