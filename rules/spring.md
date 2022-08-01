# Clean Code Rules for the Spring Framework

## Context

<a id="context-no-final-components"></a>
### Spring beans should not be final
<sup>`Rule-ID: spring.context-no-final-components`</sup>

Classes that will be picked up as Spring Beans (`@Component`, `@Service` or similar) must not be final as final
classes cannot be proxied (this is required in case you are using Spring AOP for things like `@Transactional`).

<a id="context-cacheable-annotated-functions-should-not-be-called-from-function-in-same-class"></a>
### Functions annotated with @Cacheable, @CachePut or @CacheEvict should not be called from the same class
<sup>`Rule-ID: spring.context-cacheable-annotated-functions-should-not-be-called-from-function-in-same-class`</sup>

In order for annotations to work properly it has to be called from outside of the class.
Calling functions internally will ignore the annotations. In this case the cacheables will be ignored.

## Transaction Handling

In Spring-Boot based Java web projects the `@Transactional` annotation is typically used for transaction demarcation.
There are various best-practices as well as several potential pitfalls out there:

- Transactions with Spring and JPA
- Understanding transaction pitfalls
- Spring pitfalls: proxying
- The goal is to provide a Cloudflight wide best-practice that is based on our needs and experienced problems from the past.

We want to:

- Minimize the chance for unwanted side effects
- Minimize the chance for performance issues at runtime
- Minimize the chance for unwanted non-transactional behaviour and data corruption
- Possibility to provide ArchUnit checks

### Only annotate concrete classes with @Transactional

Springs annotation based declarative transaction management uses AOP-proxies (see Understanding the Spring Framework’s
Declarative Transaction Implementation for further information). We want to follow the recommendation of the Spring
team as stated in using `@Transactional`:

> The Spring team recommends that you annotate only concrete classes (and methods of concrete classes) with the `@Transactional`
> annotation, as opposed to annotating interfaces. You certainly can place the `@Transactional` annotation on an interface
> (or an interface method), but this works only as you would expect it to if you use interface-based proxies.
> The fact that Java annotations do not inherit from interfaces means that, if you use class-based proxies
> (`proxy-target-class="true"`) or the weaving-based aspect (`mode="aspectj"`), the transaction settings are not
> recognized by the proxying and weaving infrastructure, and the object is not wrapped in a transactional proxy.


See also Github Spring-Projects [issue 18894](https://github.com/spring-projects/spring-framework/issues/18894)
and [issue 5423](https://github.com/spring-projects/spring-boot/issues/5423).

<a id="tx-no-transactional-on-classlevel"></a>
### No @Transactional annotations on class level
<sup>`Rule-ID: spring.tx-no-transactional-on-classlevel`</sup>

It is compelling to just put the `@Transactional` on the class level, but a class-level annotation does not apply to
ancestor classes up the class hierarchy; in such a scenario, methods need to be locally redeclared in order to participate
in a subclass-level annotation. This can sometimes be troublesome when working with class-based proxies. Managing annotations
on a class-level is also much more prone to side effects (see Pitfall #1), especially if your work in a shared codebase.
Since the annotation changes the behavior of the code the scope that is affected by this change should be as small as possible.

Annotation inheritance in Java is only supported for type-level annotations. If type- and method-level annotations are
mixed the actual behavior might differ from the expected as soon as you start overloading methods.
Decision driver #1 is to minimize the chance of side effects, having only method-level annotations bears the least
risk for such side effects, since they are never inherited and always have to be stated explicitly.

It is also considered best practice by others
(see [Spring @Transactional Annotation class or method](https://stackoverflow.com/questions/29027788/spring-transactional-annotation-class-or-method)).

<a id="tx-controller-methods-should-not-be-transactional"></a>
### Transaction demarcation is part of the service layer
<sup>`Rule-ID: spring.tx-controller-methods-should-not-be-transactional`</sup>

Even if it is tempting to open a transaction in the controller this can have a huge performance impact and might
even make your application prone to denial-of-service attacks. Depending on the order of the different aspects applied
to your controller data binding, input validation, authentication, and authorization might already be part of the
transaction, meaning even unauthorized or fraudulent requests require a database connection. If you serialize
complex datastructures or transfer larger amounts of data back to the user over a slow network, such
operations might keep transactions open way longer than necessary. These are typical causes for
spontaneous pool exhaustion or connection problems which you typically don't experience on your development
machine or in isolated test environments. We therefore prohibit @Transactional on controller methods.

<a id="tx-controller-methods-should-not-access-more-than-one-transactional-method"></a>
### Controller methods should not access more than one transactional method
<sup>`Rule-ID: spring.tx-controller-methods-should-not-access-more-than-one-transactional-method`</sup>

The downside of the transaction demarcation rule from above is, that you might call multiple service methods within a single controller method causing
potential database operations to be executed independently in separate transactions. This might be fine if you
just read data to fill the view-model, but it might as well be problematic if accessed data is
depending on each other. To minimize this pitfall we require that only one service call can be made within a
single controller method.

<a id="tx-transactional-methods-should-access-other-transactional-methods-or-repositories"></a>
### Transactional methods should access other transactional methods or Repositories
<sup>`Rule-ID: spring.tx-transactional-methods-should-access-other-transactional-methods-or-repositories`</sup>

Being inside a transactional context is costly and should only be done if you access other transactional resources
or the database itself via repositories. If there is no need to open a transaction, don't do it.

<a id="tx-repository-only-from-transactional-methods"></a>
### Repositories should only be called from transactional methods
<sup>`Rule-ID: spring.tx-repository-only-from-transactional-methods`</sup>

Whenever you access the database via Spring Data repositories, ensure you are within a transactional (i.e. it is called
from a method that is annotated with `@Transactional`) to ensure ACID criteria when reading data from the DB. In case
you are only reading from the database (and not writing), consider to use `@Transactional(readOnly=true)`

<a id="tx-transactional-methods-should-not-be-cacheable"></a>
### Functions should not be  @Transactional and @Cacheable
<sup>`Rule-ID: spring.tx-transactional-methods-should-not-be-cacheable`</sup>

The `@Cacheable` Annotation enables a caching optimization for the corresponding function call.
When applied the function may return a cached result from an earlier call, instead of actually evaluating the function.
Hence, a `@Cacheable` function should not have side effects!

The `@Transactional` annotation opens database transactions.
Transactions bundle several write operations so that they apply atomically. In case of an error, they will be rolled back collectively.

<a id="tx-do-not-throw-exceptions"></a>
### @Transactional annotated methods must not throw checked exceptions
<sup>`Rule-ID: spring.tx-do-not-throw-exceptions`</sup>

By default, only `RuntimeException` triggers a rollback of the current transaction, see
[declarative rolling back](https://docs.spring.io/spring/docs/5.2.3.RELEASE/spring-framework-reference/data-access.html#transaction-declarative-rolling-back).

<a id="tx-no-javax-transactions-transactional-annotations"></a>
### @Transactional from javax.transactional.Transactional should not be used
<sup>`Rule-ID: spring.tx-no-javax-transactions-transactional-annotations`</sup>

The Spring @Transactional annotation has more options than the javax @Transactional annotation and hence should be preferred.

## Spring Web

<a id="web-no-request-mapping-on-interface-top-level"></a>
### Do not put @RequestMapping on top-level interface declarations
<sup>`Rule-ID: spring.web-no-request-mapping-on-interface-top-level`</sup>

Since [Spring Cloud 2021.0.0](https://github.com/spring-cloud/spring-cloud-openfeign/commit/d6783a6f1ec8dd08fafe76ecd072913d4e6f66b9#diff-4a0384ff3e4b36193d99940fe6fbcacf9d50cfa650bddf9c40c8fe064c9c0131) it is not supported anymore to create declarative Feign clients of interfaces which
have a `@RequestMapping` annotation on the top level, that means that you can't create a Feign client from that interface any more:

````kotlin
@Api
@RequestMapping("/hello")
interface MyApi {

    @GetMapping("/world")
    fun world() : String
}
````

Instead, you need to merge that top-level annotation into the method annotations:

````kotlin
@Api
interface MyApi {

    @GetMapping("/hello/world")
    fun world() : String
}
````

or if you wanna keep the root context path a central constant:

````kotlin
@Api
interface MyApi {

    @GetMapping("$CONTEXT_PATH/world")
    fun world() : String

    companion object {
        private const val CONTEXT_PATH = "/hello"
    }
}
````

and in Java:

````java
@Api
interface MyApi {

    String CONTEXT_PATH = "/hello";

    @GetMapping(CONTEXT_PATH + "/world")
    String world();
}
````