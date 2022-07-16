package io.cloudflight.cleancode.archunit.rules.logging.public2

import mu.KotlinLogging

class MyClassWithPublicLogger2 {

    fun foo() {
        LOG.info("")
    }

    companion object {
        val LOG = KotlinLogging.logger { }
    }
}