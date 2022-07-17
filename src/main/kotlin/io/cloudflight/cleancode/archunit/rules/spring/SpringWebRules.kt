package io.cloudflight.cleancode.archunit.rules.spring

import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import io.cloudflight.cleancode.archunit.ArchRuleWithId.Companion.archRuleWithId
import org.springframework.web.bind.annotation.RequestMapping

class SpringWebRules {

    @ArchTest
    val requestmapping_is_not_allowed_on_top_level =
        archRuleWithId(
            "spring.web-no-request-mapping-on-interface-top-level",
            noClasses()
                .that()
                .areInterfaces()
                .should()
                .beAnnotatedWith(RequestMapping::class.java)
        )
}
