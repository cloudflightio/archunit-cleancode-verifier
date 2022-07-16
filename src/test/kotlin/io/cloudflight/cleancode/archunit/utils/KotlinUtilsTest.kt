package io.cloudflight.cleancode.archunit.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.persistence.Entity

class KotlinUtilsTest {

    @Test
    fun stringIsNotKotlin() {
        assertThat(java.lang.String::class.java.isKotlinClass()).isFalse
    }

    @Test
    fun kotlinIsKotlin() {
        assertThat(KotlinUtilsTest::class.java.isKotlinClass()).isTrue
    }

    @Test
    fun kotlinClassFromOtherModuleIsKotlin() {
        assertThat(kotlin.random.Random::class.java.isKotlinClass()).isTrue
    }

    @Test
    fun internalKotlinClassIsKotlin() {
        assertThat(InternalTestClass::class.java.isKotlinClass()).isTrue
    }

    @Test
    fun internalEntityClassIsKotlin() {
        assertThat(InternalEntity::class.java.isKotlinClass()).isTrue
    }
}


internal class InternalTestClass {

}

@Entity
internal class InternalEntity {

}
