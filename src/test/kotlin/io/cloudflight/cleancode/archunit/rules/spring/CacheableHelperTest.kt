package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.cache.annotation.Cacheable

internal class CacheableHelperTest {

    @Test
    fun `isCacheable(JavaCodeUnit,false,false) should return True if CodeUnit has Cacheable Annotation`() {
        val testClass = ClassFileImporter().importClass(TestRepository1::class.java)

        testClass.methods.forEach {
            assertTrue(SpringTransactionalRules.CacheableHelper.isCacheable(it, checkOwner = false, checkSubClasses = false))
        }
    }

    @Test
    fun `isCacheable(JavaCodeUnit,false,false) should return False if CodeUnit has not Cacheable Annotation`() {
        val testClass = ClassFileImporter().importClass(TestRepository3::class.java)

        testClass.methods.forEach {
            assertFalse(SpringTransactionalRules.CacheableHelper.isCacheable(it, checkOwner = false, checkSubClasses = false))
        }
    }

    @Test
    fun `isCacheable(JavaCodeUnit,true,false) should return True if owner of CodeUnit has Cacheable Annotation`() {
        val testClass = ClassFileImporter().importClass(TestRepository3::class.java)

        testClass.methods.forEach {
            assertTrue(SpringTransactionalRules.CacheableHelper.isCacheable(it, checkOwner = true, checkSubClasses = false))
        }
    }

    @Test
    fun `isCacheable(JavaCodeUnit,true,false) should return False if owner of CodeUnit has not Cacheable Annotation`() {
        val testClass = ClassFileImporter().importClass(TestRepository4::class.java)

        testClass.methods.forEach {
            assertFalse(SpringTransactionalRules.CacheableHelper.isCacheable(it, checkOwner = true, checkSubClasses = false))
        }
    }

    @Test
    fun `(CacheableAnnotation apply) should return True if a class with Cacheable Annotation is processed`() {
        val testClass = ClassFileImporter().importClass(TestRepository3::class.java)
        assertTrue(testClass.isAnnotatedWith(SpringTransactionalRules.CacheableHelper.CacheableAnnotation()))
    }

    @Test
    fun `(CacheableAnnotation apply) should return False if a class without Cacheable Annotation is processed`() {
        val testClass = ClassFileImporter().importClass(TestRepository4::class.java)
        assertFalse(testClass.isAnnotatedWith(SpringTransactionalRules.CacheableHelper.CacheableAnnotation()))
    }

    @Test
    fun `isCacheable(JavaCodeUnit,true,true) should pass if classes implementing given Interface have a method with Cacheable Annotation`() {
        val testClasses = ClassFileImporter().importClasses(
                TestRepository1::class.java,
                TestRepository2::class.java,
                TestRepository3::class.java,
                TestRepository4::class.java,
                TestRepository4Impl::class.java)

        testClasses.get(TestRepository4::class.java).methods.forEach {
            assertTrue(SpringTransactionalRules.CacheableHelper.isCacheable(it,checkOwner = true, checkSubClasses = true))
        }
    }

    @Test
    fun `isCacheable(JavaCodeUnit,true,true) should fail if class and subclass have not Cacheable Annotation`() {
        val testClasses = ClassFileImporter().importClasses(
                TestRepository1::class.java,
                TestRepository2::class.java,
                TestRepository3::class.java,
                TestRepository4::class.java,
                TestRepository4Impl::class.java,
                TestRepository6::class.java,
                TestRepository6Impl::class.java)
        val testClass = ClassFileImporter().importClass(TestRepository6::class.java)


        testClass.methods.forEach {
            assertFalse(SpringTransactionalRules.CacheableHelper.isCacheable(it, checkOwner = true, checkSubClasses = true))
        }
    }
}


@Suppress("SpringCacheNamesInspection")
open class TestRepository1(private val anotherRepo: TestRepository2) {
    @Cacheable
    open fun foo() {
        this.anotherRepo.anotherCacheableMethod()
    }
}

@Suppress("SpringCacheNamesInspection", "SpringCacheAnnotationsOnInterfaceInspection")
interface TestRepository2 {
    @Cacheable
    fun anotherCacheableMethod()
}

@Suppress("SpringCacheNamesInspection", "SpringCacheAnnotationsOnInterfaceInspection")
@Cacheable
interface TestRepository3 {
    fun foo()
}

interface TestRepository4 {
    fun foo()
}

@Suppress("SpringCacheNamesInspection")
open class TestRepository4Impl : TestRepository4 {
    @Cacheable
    override fun foo() {
    }
}

interface TestRepository6 {
    fun foo()
}

class TestRepository6Impl : TestRepository6 {
    override fun foo() {}
}
