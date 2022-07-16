# Clean Code Rules for Logging

<a id="no-streams"></a>
### Do not use standard io streams
<sup>`Rule-ID: logging.no-streams`</sup>

Do not use classes and methods like `System.out.println()` or `ex.printStacktrace()`, but instead
use `org.slf4j.Logger`. This ensures that all log messages go to the same file and enables you
to have managed logging by selectively turning on and off logging. An example of logging done right
looks like the following:

````kotlin
package io.cloudflight.my_fancy_project.service

import mu.KotlinLogging

@Service
class MyService{

    fun myFunction(){
        LOG.debug("my debug message")
    }

    companion object {
        private val LOG = KotlinLogging.logger {}
    }
}
````

If you do not see your output on stdout, your active loglevel might be the cause. Adapt your `logback-spring.xml` in that case.

<a id="no-java-util-logging"></a>
### Do not use Java Util Logging
<sup>`Rule-ID: logging.no-java-util-logging`</sup>

Do not use Java Util Logging for your loggers, but instead Slf4J.

<a id="static-final-loggers"></a>
### Loggers should be private static final
<sup>`Rule-ID: logging.static-final-loggers`</sup>

Instances of `org.slf4j.Logger` should be `private`, `static` and `final`.

<a id="do-not-expose-loggers-via-methods"></a>
### Do not expose loggers via public methods
<sup>`Rule-ID: logging.do-not-expose-loggers-via-methods`</sup>

Instances of `org.slf4j.Logger` should not be made accessible via public methods and therefore exposed. They
should stay private within your classes. If you make loggers accessible to other objects, it is really
hard to read and filter logs accordingly.

This violation is also triggered if you have your logger as non-private `val` in your Kotlin `companion object`:

````kotlin
class MyService{

    fun myFunction(){
        LOG.debug("my debug message")
    }

    companion object {
        val LOG = KotlinLogging.logger {}
    }
}
````

Make the logger `private` here to not make it accessible from outside.

<a id="do-not-extend-klogging"></a>
### Do not extend from mu.KLogging
<sup>`Rule-ID: logging.do-not-extend-klogging`</sup>

Although the documentation of KLogging tells you to write code like this:

````kotlin
class MyClassWithPublicLogger3 {

    fun foo() {
        logger.info("")
    }

    companion object : KLogging() {
    }
}
````

We advise not to do that, as `mu.KLogging` also exposes the logger as public `val` and therefore again
makes the logger accessible to any other object.
