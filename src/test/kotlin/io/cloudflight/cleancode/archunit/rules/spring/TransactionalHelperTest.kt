package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.domain.AccessTarget
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import io.cloudflight.cleancode.archunit.rules.spring.TransactionalHelper.isTransactionalFunctionOrInRepositoryClass
import io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase.Service
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional

internal class TransactionalHelperTest {

    @Test
    fun `isTransactional(AccessTarget) with only method calls which are also Transactional should pass`() {
        val testClasses = ClassFileImporter().importClasses(MyRepository1::class.java)


        val testClass = ClassFileImporter().importClass(MyRepository1::class.java)
        val targets = getMethodCallsInMethodsFromClass(testClass)
        targets.forEach { assertTrue(TransactionalHelper.isTransactional(it)) }
    }

    private fun getMethodCallsInMethodsFromClass(testClass: JavaClass) =
        testClass.methods.map {
            it.accessesFromSelf
                .filter { javaMethodCall -> javaMethodCall.target is AccessTarget.MethodCallTarget }
                .map { javaMethodCall -> javaMethodCall.target }
        }.flatten()

    @Test
    fun `isTransactional(JavaCodeUnit,false,false) should return True if CodeUnit has Transactional Annotation`() {
        val testClass = ClassFileImporter().importClass(MyRepository1::class.java)

        testClass.methods.forEach {
            assertTrue(
                TransactionalHelper.isTransactional(
                    it,  checkOwner = false, checkSubClasses = false
                )
            )
        }
    }

    @Test
    fun `isTransactional(JavaCodeUnit,false,false) should return False if CodeUnit has not Transactional Annotation`() {
        val testClass = ClassFileImporter().importClass(MyRepository3::class.java)

        testClass.methods.forEach {
            assertFalse(
                TransactionalHelper.isTransactional(
                    it,  checkOwner = false, checkSubClasses = false
                )
            )
        }
    }

    @Test
    fun `isTransactional(JavaCodeUnit,true,false) should return True if owner of CodeUnit has Transactional Annotation`() {
        val testClass = ClassFileImporter().importClass(MyRepository3::class.java)

        testClass.methods.forEach {
            assertTrue(
                TransactionalHelper.isTransactional(
                    it, checkOwner = true, checkSubClasses = false
                )
            )
        }
    }

    @Test
    fun `isTransactional(JavaCodeUnit,true,false) should return False if owner of CodeUnit has not Transactional Annotation`() {
        val testClass = ClassFileImporter().importClass(MyRepository4::class.java)

        testClass.methods.forEach {
            assertFalse(
                TransactionalHelper.isTransactional(
                    it,  checkOwner = true, checkSubClasses = false
                )
            )
        }
    }

    @Test
    fun `(TransactionalAnnotation apply) should return True if a class with Transactional Annotation is processed`() {
        val testClass = ClassFileImporter().importClass(MyRepository3::class.java)
        assertTrue(testClass.isAnnotatedWith(TransactionalHelper.TransactionalAnnotation()))
    }

    @Test
    fun `(TransactionalAnnotation apply) should return False if a class without Transactional Annotation is processed`() {
        val testClass = ClassFileImporter().importClass(MyRepository4::class.java)
        assertFalse(testClass.isAnnotatedWith(TransactionalHelper.TransactionalAnnotation()))
    }

    @Test
    fun `(TransactionalAnnotation apply) should return True if a class with JavaxTransactional Annotation is processed`() {
        val testClass = ClassFileImporter().importClass(MyRepository5::class.java)
        assertTrue(testClass.isAnnotatedWith(TransactionalHelper.JavaxTransactionalAnnotation()))
    }

    @Test
    fun `(TransactionalAnnotation apply) should return False if a class without JavaxTransactional Annotation is processed`() {
        val testClass = ClassFileImporter().importClass(MyRepository3::class.java)
        assertFalse(testClass.isAnnotatedWith(TransactionalHelper.JavaxTransactionalAnnotation()))
    }


    @Test
    fun `isTransactional(JavaCodeUnit,true,true) should pass if classes implementing given Interface have a method with Transactional Annotation`() {
        val testClasses = ClassFileImporter().importClasses(
            MyRepository1::class.java,
            MyRepository2::class.java,
            MyRepository3::class.java,
            MyRepository4::class.java,
            MyRepository4Impl::class.java,
            MyRepository5::class.java
        )

        testClasses.get(MyRepository4::class.java).methods.forEach {
            assertTrue(TransactionalHelper.isTransactional(it, checkOwner = true, checkSubClasses = true))
        }
    }

    @Test
    fun `isTransactional(JavaCodeUnit,true,true) should fail if class and subclass have not Transactional Annotation`() {
        val testClasses = ClassFileImporter().importClasses(
            MyRepository1::class.java,
            MyRepository2::class.java,
            MyRepository3::class.java,
            MyRepository4::class.java,
            MyRepository4Impl::class.java,
            MyRepository5::class.java,
            MyRepository6::class.java,
            MyRepository6Impl::class.java
        )
        val testClass = ClassFileImporter().importClass(MyRepository6::class.java)

        testClass.methods.forEach {
            assertFalse(TransactionalHelper.isTransactional(it, checkOwner = true, checkSubClasses = true))
        }
    }

    @Test
    @Disabled // TODO enable again
    fun extensionFunctionsOnRepositories() {
        val testClasses = ClassFileImporter().importClasses(
            Service::class.java
        )
        val clazz =
            testClasses.get(Service::class.java)
        val method = clazz.methods.first { it.name.contains("callRepoExtensionFunction") }
        val access = method.accessesFromSelf.stream().findFirst().get()
        assertThat(access.isTransactionalFunctionOrInRepositoryClass()).isTrue
    }
}


open class MyRepository1(private val anotherRepo: MyRepository2) {
    @Transactional
    open fun foo() {
        this.anotherRepo.anotherTransactionalMethod()
    }
}

interface MyRepository2 {
    @Transactional
    fun anotherTransactionalMethod()
}

@Transactional
interface MyRepository3 {
    fun foo()
}

interface MyRepository4 {
    fun foo()
}

open class MyRepository4Impl : MyRepository4 {
    @Transactional
    override fun foo() {
    }
}

@javax.transaction.Transactional
interface MyRepository5 {
    fun foo()
}

interface MyRepository6 {
    fun foo()
}

class MyRepository6Impl : MyRepository6 {
    override fun foo() {}
}
