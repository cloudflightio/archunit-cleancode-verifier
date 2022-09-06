package io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase

import io.cloudflight.cleancode.archunit.rules.spring.txtest.badcase.MyRepository2
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Controller
class Controller(
    private val service: io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase.Service,
    private val repo: MyRepository
) : ControllerInterface {

    @GetMapping
    fun foo() {
        service.callRepo()
    }

    @GetMapping
    fun doesNotRequireTransaction() {
        service.callRepo()
    }

    @GetMapping
    fun callPrivateMethod() {
        privateMethodAndReturn("tt")
    }

    @GetMapping
    override fun interfaceMethod() {
        service.callRepo()
    }

    private fun privateMethodAndReturn(foo: String): String {
        repo.findAll()
        return foo
    }
}

interface ControllerInterface {
    fun interfaceMethod()
}

@Controller
class Controller2(
    private val service: io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase.Service
) {
    @GetMapping
    //@DoesNotRequireTransactional
    fun foo() {
        service.callRepo()
        service.callRepo()
    }
}

@Service
class Service(private val repo: MyRepository, private val lockService: LockService, private val repo2: MyRepository2) {

    @Transactional
    fun callRepo() {
        repo.findAll()
    }

    @Transactional
    fun callRepoWithDefaultMethod() {
        repo.findByName()
    }

    @Transactional
    fun callRepoWithDefaultMethod(name: String) {
        repo.findByName(name)
    }

    // @RequiresTransactional // TODO ArchUnit cannot deal correctly with Consumers
    @Transactional
    fun lambda() {
        getOptional().ifPresent {
            privateCallRepo()
        }
    }

    @Transactional
    fun callRepoExtensionFunction(name:String) {
        repo.findByIdOrNull(name)
    }

    private fun getOptional(): Optional<String> {
        return Optional.empty()
    }

    @Transactional
    fun callRepoInLambda() {
        listOf(1, 2).forEach { repo.findAll() }
    }

    @Transactional
    fun callPrivateMethodInLambda() {
        listOf(1, 2).forEach { privateCallRepo() }
    }


    private fun privateCallRepo() {
        repo.findAll()
    }


    @Transactional(readOnly = true)
    fun readOnlyTransaction() {
        repo.findAll()
    }

    //@RequiresTransactional // TODO ArchUnit cannot deal correctly with Consumers
    @Transactional
    fun callRepoInRunnable() {
        lockService.runWithLock("any") {
            repo.findAll()
            LoggerFactory.getLogger("").info("")
        }
    }
}

interface MyRepository : JpaRepository<GoodEntity, String> {
    fun findByName(name: String = "john")
}

@Entity
class GoodEntity(
    @Id
    @field:NotNull
    val id: String,
    @field:NotNull
    val name: String
)


@Service
class ServiceWithoutJavaxTransactional(private val repo: MyRepository) {

    @Transactional
    fun callRepo() {
        repo.findAll()
    }
}


@Service
class ServiceWithCacheable {
    @Cacheable("Cacheable test method")
    fun cacheableFunction() {
    }
}

@Service
class ServiceWithCachePut {
    @CachePut("Cacheable test method")
    fun cachePutFunction() {
    }
}

@Service
class ServiceWithCacheEvict {
    @CacheEvict("Cacheable test method")
    fun cacheEvictFunction() {
    }
}

@Service
class ServiceCallingCachePut(private val serviceWithCachePut: ServiceWithCachePut) {
    fun cacheEvictFunction() {
        this.serviceWithCachePut.cachePutFunction()
    }
}


@Service
class ServiceCallingCacheEvict(private val serviceWithCacheEvict: ServiceWithCacheEvict) {
    fun cacheEvictFunction() {
        this.serviceWithCacheEvict.cacheEvictFunction()
    }
}


@Service
class ServiceCallingCacheable(private val serviceWithCacheable: ServiceWithCacheable) {
    fun cacheEvictFunction() {
        this.serviceWithCacheable.cacheableFunction()
    }
}

class SomeService() {
    fun something() {
        val enumValues = Test.values()
        println(enumValues)
    }
}

enum class Test {
    Test1, Test2;
}

@RestController
class SomeController(private val someService: SomeService) {
    fun test() {
        someService.something()
    }
}
