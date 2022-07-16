package io.cloudflight.cleancode.archunit.rules.logging.public3

import mu.KLogging

class MyClassWithPublicLogger3 {

    fun foo() {
        logger.info("")
    }

    companion object : KLogging()
}
