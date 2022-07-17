package io.cloudflight.cleancode.archunit.utils

import com.tngtech.archunit.junit.ArchTests

fun Class<*>.isKotlinClass(): Boolean {
    return this.declaredAnnotations.any {
        it.annotationClass == Metadata::class
    }
}

internal fun ruleSetOf(clazz: Class<*>, vararg requiredClasses: String): ArchTests {
    return if (classesExistOnClasspath(*requiredClasses)) {
        ArchTests.`in`(clazz)
    } else {
        ArchTests.`in`(Object::class.java)
    }
}

internal fun classesExistOnClasspath(vararg classes: String): Boolean {
    classes.forEach {
        try {
            Class.forName(it)
        } catch (err: ClassNotFoundException) {
            return false
        }
    }
    return true
}



