package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional

internal class JavaMethodHelperTest {

    // Had to import every Class used otherwise the methods would be empty
    private val testClasses: JavaClasses = ClassFileImporter().importClasses(
        MyTransitivCallsClassLevelOne::class.java,
        MyTransitivCallsClassLevelTwo::class.java,
        MyTransitivCallsClassLevelThree::class.java,
        MyTransitivCallsClassLevelFour::class.java
    )

    @Test
    fun `collectTransitiveCalls(JavaMethod, false) should return every function call once`() {
        val result = JavaMethodHelper.collectTransitiveCalls(
            testClasses.get(MyTransitivCallsClassLevelOne::class.java).getMethod("functionLevelOneFirst")
        )
        assertThat(result.size).isEqualTo(8)
    }

    @Test
    fun `collectTransitiveCalls(JavaMethod, true) should return every function call once until a function with transactional is found`() {
        val result = JavaMethodHelper.collectTransitiveCalls(
            testClasses.get(MyTransitivCallsClassLevelOne::class.java).getMethod("functionLevelOneFirst"),
            true
        )
        assertThat(result.size).isEqualTo(5)
    }
}


class MyTransitivCallsClassLevelOne(private val myTransitivCallsClassLevelTwo: MyTransitivCallsClassLevelTwo) {

    fun functionLevelOneFirst() {
        this.functionLevelOneSnd()
        myTransitivCallsClassLevelTwo.functionLevelTwoFirst()
        myTransitivCallsClassLevelTwo.functionLevelTwoFirst()
        myTransitivCallsClassLevelTwo.functionLevelTwoSnd()
    }

    private fun functionLevelOneSnd() {}
}


open class MyTransitivCallsClassLevelTwo(private val myTransitivCallsClassLevelThree: MyTransitivCallsClassLevelThree) {
    @Transactional
    open fun functionLevelTwoFirst() {
        myTransitivCallsClassLevelThree.functionLevelThreeFirst()
        this.functionLevelTwoSnd()
        this.functionLevelTwoThird()
        this.functionLevelTwoThird()
    }

    fun functionLevelTwoSnd() {
        myTransitivCallsClassLevelThree.functionLevelThreeFirst()
    }

    fun functionLevelTwoThird() {
    }
}


open class MyTransitivCallsClassLevelThree(private val myTransitivCallsClassLevelFour: MyTransitivCallsClassLevelFour) {
    @Transactional
    open fun functionLevelThreeFirst() {
        myTransitivCallsClassLevelFour.functionLevelFourFirst()
    }
}


abstract class MyTransitivCallsClassLevelFour {
    abstract fun functionLevelFourFirst()
}
