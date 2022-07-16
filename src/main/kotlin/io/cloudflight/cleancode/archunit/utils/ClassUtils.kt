package io.cloudflight.cleancode.archunit.utils

fun Class<*>.isKotlinClass(): Boolean {
    return this.declaredAnnotations.any {
        it.annotationClass == Metadata::class
    }
}



