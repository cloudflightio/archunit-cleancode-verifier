package io.cloudflight.cleancode.archunit.rules.logging.private1

import mu.KotlinLogging
import org.slf4j.LoggerFactory

class MyClassWithPrivateLogger1 {

    fun foo() {
    }

    companion object {
        private val LOG = KotlinLogging.logger { }
        private val LOG2 = LoggerFactory.getLogger(MyClassWithPrivateLogger1::class.java)
    }
}
