plugins {
    `maven-publish`
    alias(libs.plugins.nexuspublish)
    signing
}

description = "ArchUnit Rules for various JVM frameworks"
group = "io.cloudflight.cleancode.archunit"

autoConfigure {
    java {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendorName.set("Cloudflight")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.archunit.junit5)

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    compileOnly(libs.jakarta.persistence.api)
    compileOnly(libs.jakarta.validation.api)
    compileOnly(libs.jakarta.transaction.api)
    compileOnly(libs.spring.context)
    compileOnly(libs.spring.tx)
    compileOnly(libs.spring.web)
    compileOnly(libs.spring.data.jpa)
    compileOnly(libs.spring.boot)

    testImplementation(libs.junit)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)

    testImplementation(libs.jakarta.persistence.api)
    testImplementation(libs.jakarta.validation.api)
    testImplementation(libs.jakarta.transaction.api)
    testImplementation(libs.spring.context)
    testImplementation(libs.spring.tx)
    testImplementation(libs.spring.web)
    testImplementation(libs.spring.data.jpa)
    testImplementation(libs.spring.boot)

    testImplementation(libs.kotlin.logging)
}

java {
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/cloudflightio/archunit-cleancode-verifier")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                inceptionYear.set("2022")
                organization {
                    name.set("Cloudflight")
                    url.set("https://cloudflight.io")
                }
                developers {
                    developer {
                        id.set("cloudflightio")
                        name.set("Cloudflight Team")
                        email.set("opensource@cloudflight.io")
                    }
                }
                scm {
                    connection.set("scm:ggit@github.com:cloudflightio/archunit-cleancode-verifier.git")
                    developerConnection.set("scm:git@github.com:cloudflightio/archunit-cleancode-verifier.git")
                    url.set("https://github.com/cloudflightio/archunit-cleancode-verifier")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}

signing {
    setRequired {
        System.getenv("PGP_SECRET") != null
    }
    useInMemoryPgpKeys(System.getenv("PGP_SECRET"), System.getenv("PGP_PASSPHRASE"))
    sign(publishing.publications.getByName("maven"))
}
