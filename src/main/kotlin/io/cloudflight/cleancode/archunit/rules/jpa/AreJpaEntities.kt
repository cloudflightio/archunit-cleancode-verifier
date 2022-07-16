package io.cloudflight.cleancode.archunit.rules.jpa

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import javax.persistence.Embeddable
import javax.persistence.Entity

class AreJpaEntities : DescribedPredicate<JavaClass>("are JPA Entity classes") {
    override fun test(input: JavaClass): Boolean {
        return input.isAnnotatedWith(Entity::class.java) || input.isAnnotatedWith(Embeddable::class.java)
    }
}