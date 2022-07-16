package io.cloudflight.cleancode.archunit.rules.logging.private2

import io.cloudflight.cleancode.archunit.rules.logging.private1.MyClassWithPrivateLogger1
import org.slf4j.LoggerFactory

object MyClassWithPrivateLogger2 {

    private val LOG = LoggerFactory.getLogger(MyClassWithPrivateLogger1::class.java)
}