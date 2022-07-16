package io.cloudflight.cleancode.archunit.rules.logging.public5

import mu.KotlinLogging

class MyClassWithPublicLogger5 {

    val LOG = KotlinLogging.logger {  }

    fun foo() {
        LOG.info("")
    }

}