package io.cloudflight.cleancode.archunit.rules.logging.public1

import org.slf4j.LoggerFactory

class MyClassWithPublicLogger {

    fun foo() {
        LOG.info("")
    }

    companion object {
        val LOG = LoggerFactory.getLogger("")
    }
}