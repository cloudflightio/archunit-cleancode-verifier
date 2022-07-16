package io.cloudflight.cleancode.archunit.utils

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import org.slf4j.LoggerFactory

class AreKotlinClasses : DescribedPredicate<JavaClass>("are kotlin classes") {
    override fun test(input: JavaClass): Boolean {
        return try {
            input.reflect().isKotlinClass()
        } catch (err: NoClassDefFoundError) {
            LOG.error("Cannot detect if $input is a KotlinClass due to $err. Assuming it's not a Kotlin class")
            false
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AreKotlinClasses::class.java)
    }
}