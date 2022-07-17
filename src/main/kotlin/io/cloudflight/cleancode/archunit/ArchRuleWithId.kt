package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.freeze.FreezingArchRule.freeze

internal class ArchRuleWithId(val id: String, rule: ArchRule) :
    ArchRule by rule.because(idToUrl(id)).allowEmptyShould(true) {

    companion object {
        fun archRuleWithId(id: String, delegate: ArchRule): ArchRule {
            return if (FREEZE_ENABLED) {
                freeze(ArchRuleWithId(id, delegate)).persistIn(IdBasedViolationStore)
            } else {
                ArchRuleWithId(id, delegate)
            }
        }

        internal fun idToUrl(id: String): String {
            return "$ROOT_URL/${id.substringBefore(".")}.md#user-content-${id.substringAfter(".")}"
        }

        private val VERSION = "v${ArchRuleWithId::class.java.`package`.implementationVersion}"
        private val ROOT_URL = "https://github.com/cloudflightio/archunit-cleancode-verifier/blob/$VERSION/rules"

        internal var FREEZE_ENABLED = true
    }
}
