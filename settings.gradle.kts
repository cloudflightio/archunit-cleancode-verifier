plugins {
    id("io.cloudflight.autoconfigure-settings") version "0.9.1"
}

rootProject.name = "archunit-cleancode-verifier"

configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}