package io.cloudflight.cleancode.archunit.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClassUtilsTest {

    @Test
    fun existOnClassPath() {
        assertThat(classesExistOnClasspath("java.lang.Object")).isTrue
    }

    @Test
    fun dontExistOnClassPath() {
        assertThat(classesExistOnClasspath("io.cloudflight.Foo")).isFalse
    }

    @Test
    fun dontExistOnClassPath2() {
        assertThat(classesExistOnClasspath("java.lang.Object", "io.cloudflight.Foo")).isFalse
    }

}