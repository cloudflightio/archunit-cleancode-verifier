 # CleanCode Verifiers powered by ArchUnit

[![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.cloudflight.cleancode.archunit/archunit-cleancode-verifier.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.cloudflight.cleancode.archunit/archunit-cleancode-verifier)

# Motivation 

Static code analysis is important, and there exist plenty of great tools
out there like FindBugs, PMD or Detekt.

Unfortunately, all those rules have their limit, they are all not
analyzing the whole code base, but only class files in an isolated way.

This is where the great library [ArchUnit](https://github.com/TNG/ArchUnit) comes into 
the game. This library uses ArchUnit and provides
a collection of `ArchRule`s which we consider best-practice
at @cloudflightio. 

Those are grouped in:

* [Extended Rules for the JDK](rules/jdk.md)
* [Java Logging](rules/logging.md)
* [Java Persistence Framework (JPA)](rules/jpa.md)

# Usage

Using this library is simple. First add it as dependency
to your test-scope, in Gradle that would be

````groovy
dependencies {
    testImplementation("io.cloudflight.cleancode.archunit:archunit-cleancode-verifier:<version>")
}
````

Use the latest version as seen in the badge on top of this page instad of `<version>`.

Then create a unit-test like this (the notation is Kotlin, but it also works with plain Java):

````kotlin
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchTests
import io.cloudflight.cleancode.archunit.CleanCodeRuleSets

@AnalyzeClasses(packagesOf = ["io.cloudflight.sample"])
class ArchitectureTest {

    @ArchTest
    val cleancode = ArchTests.`in`(CleanCodeRuleSets::class.java)
}
````

Change the package in the `AnalyzeClasses` annotation accordingly. 

That's it. Run the test now. `CleanCodeRuleSets` will automatically pick up
all tests that might be for relevance for you, i.e. JPA-related tests 
will only be executed if you have JPA on your classpath.

# How it works

Behind the scenes, we are using an own implementation of 
ArchUnit's `ViolationStore` in order to achieve a slightly different behaviour
of [Freezing Arch Rules](https://www.archunit.org/userguide/html/000_Index.html#_freezing_arch_rules).

When you run the test the first time, you will find a file
`archunit_store/knownCleanCodeRules.txt` in your module.
It contains all rules that have been evaluated. Add this file to your VCS.

In case your code violates any of the rules, you will find additional files
with the name of the rule inside that folder. Add those to your VCS as well.

* Your initial violations will be frozen not be reported. If you fix your violations,
they will be automatically removed from those files. 
* If the file is empty, the file is deleted
* If you only have the `knownCleanCodeRules.txt` in your `archunit_store` folder, you're free from violations. Congrats!
* If you wanna fix all violations immediately, remove all files but the `knownCleanCodeRules.txt`. ArchUnit will then raise errors for each of them. 
* If you want to refreeze all violations, add `freeze.refreeze=true` to your `archunit.properties` as explained in the [documentation](https://www.archunit.org/userguide/html/000_Index.html#_configuration)

You can also ignore rules entirely with [standard functionality of ArchUnit](https://www.archunit.org/userguide/html/000_Index.html#_ignoring_violations):
Create a file called `archunit_ignore_patterns.txt` in `src/test/resources` and add regular expressions
containing the names of the rules that you want to ignore (not only for existing code but also for all future code).

If finally violations are being reported, then you will see errors like that:

Suppose you have an entity like that:

````kotlin
@Entity
internal class Project(

    val name: String,

) : AbstractEntity() { // contains @Id

}
````

This violates the rule [Kotlin nullability should match JPA nullability](https://github.com/cloudflightio/archunit-cleancode-verifier/blob/v0.0.3/rules/jpa.md#user-content-nullable-flag-of-kotlin-needs-to-match-jpa-specification) and
in the log you will see something like this:

````text
java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] - Rule 'classes that are kotlin classes and are JPA Entity classes should have only members where the nullable flag of kotlin matches the JPA column specification, because https://github.com/cloudflightio/archunit-cleancode-verifier/blob/v0.0.3/rules/jpa.md#user-content-nullable-flag-of-kotlin-needs-to-match-jpa-specification' was violated (1 times):
[io.cloudflight.tracker.domain.entity.Project]: name is nullable in the DB, but the kotlin field is not nullable
	at com.tngtech.archunit.lang.ArchRule$Assertions.assertNoViolation(ArchRule.java:110)
	at com.tngtech.archunit.lang.ArchRule$Assertions.check(ArchRule.java:98)

````

Click on the link there and get to a detailled description of the violation including code samples. We believe
it's important for developers to understand the background of such issues and that usually can't be placed
into a single short String.