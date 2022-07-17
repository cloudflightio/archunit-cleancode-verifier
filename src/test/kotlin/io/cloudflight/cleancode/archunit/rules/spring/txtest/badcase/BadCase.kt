package io.cloudflight.cleancode.archunit.rules.spring.txtest.badcase

import io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase.ControllerInterface
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import javax.persistence.Entity
import javax.persistence.Id
import io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase.MyRepository as GoodcaseRepository
import io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase.Service as GoodcaseService

@Controller
class Controller(
    private val goodService: GoodcaseService,
    private val goodRepo: GoodcaseRepository,
    private val otherService: ServiceInterface
) : ControllerInterface {

    @Cacheable
    @Transactional
    fun cachedFoo() {
        goodService.callRepo()
        goodService.callRepo()
    }

    @Transactional
    @GetMapping
    fun foo() {
        goodService.callRepo()
        goodService.callRepo()
    }

    @Transactional
    @GetMapping
    override fun interfaceMethod() {
        goodService.callRepo()
        goodRepo.findAll()
    }

    @GetMapping
    fun bar() {
        bar2()
    }

    @GetMapping
    fun foobar() {
        otherService.foo()
        otherService.foo()
        goodService.callRepo()
    }

    private fun bar2() {
        goodService.callRepo()
        goodService.callRepo()
    }
}

@Service
class Service(private val repo: MyRepository) {
    fun callRepo() {
        repo.findAll()
    }
}

@Service
@Transactional
class Service2(private val repo: MyRepository) {
    fun callRepo() {
        repo.findAll()
    }
}

interface MyRepository : JpaRepository<String, String>

@Transactional
interface MyRepository2 : JpaRepository<String, String> {
    fun foo()
}

interface MyRepository3 : JpaRepository<String, String> {
    @Transactional
    fun foo()
}

interface ServiceInterface {
    fun foo()
}

@Service
class OtherServiceImpl :
    ServiceInterface {
    @Transactional
    override fun foo() {
    }
}

@Entity
class BadEntity(
    @field:Id
    val id: String
)


@Service
class ServiceWithJavaxTransactional {

    @javax.transaction.Transactional
    fun javaxTransactionalFunction() {
    }
}

@javax.transaction.Transactional
@Service
class ServicAsJavaxTransactional {}

@Service
class ServiceWithCacheable {
    @Cacheable("Cacheable test method")
    fun cacheableFunction() {
    }

    fun callCacheableFunction() {
        this.cacheableFunction()
    }
}

@Service
class ServiceWithCachePut {
    @CachePut("Cacheable test method")
    fun cachePutFunction() {
    }

    fun callCachePutFunction() {
        this.cachePutFunction()
    }
}

@Service
class ServiceWithCacheEvict {
    @CacheEvict("Cacheable test method")
    fun cacheEvictFunction() {
    }

    fun callCacheEvictFunction() {
        this.cacheEvictFunction()
    }
}

@Service
class ServiceWithRollbackFunction {

    fun functionUnsupportedOperationException() {
        throw UnsupportedOperationException()
    }

    @Transactional(rollbackFor = [Exception::class])
    fun functionThrowExceptionWithRollbackFor() {
        throw Exception()
    }
}

@Transactional(rollbackFor = [Exception::class])
class ServiceAsRollback {

    fun functionUnsupportedOperationException() {
        throw UnsupportedOperationException()
    }

    fun functionThrowExceptionWithRollbackFor() {
        throw Exception()
    }
}

@Service
class ClassWithTransactionalFunction(
    private val classWithoutTransactionalFunction: ClassWithoutTransactionalFunction
) {

    @Transactional
    fun transactionalFunction() {
        classWithoutTransactionalFunction.foo()
    }
}

class ClassWithoutTransactionalFunction {
    fun foo() {}
}
