package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.base.ArchUnitException
import com.tngtech.archunit.core.domain.JavaAnnotation
import org.springframework.core.annotation.AnnotationUtils


fun <T : Annotation> JavaAnnotation<*>.toAnnotation(
    clazz: Class<T>
): T? {
    return if (this.rawType.isEquivalentTo(clazz)) {
        this.`as`<T>(clazz)
    } else {
        try {
            AnnotationUtils.findAnnotation(this.rawType.reflect(), clazz)
        } catch (ex: ArchUnitException.ReflectionException) {
            // in very special usecases with annotation processors and transitive libraries that are only on the compileOnly
            // classpath, it can be that we cannot find the underlying class by calling rawType.reflect()
            // in that case we simply return null
            return null
        }
    }
}
