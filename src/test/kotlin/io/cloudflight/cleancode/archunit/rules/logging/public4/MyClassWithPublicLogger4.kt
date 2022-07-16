package io.cloudflight.cleancode.archunit.rules.logging.public4

import org.slf4j.LoggerFactory

class MyClassWithPublicLogger4 {

    val LOG = LoggerFactory.getLogger("")

    fun foo() {
        LOG.info("")
    }

}