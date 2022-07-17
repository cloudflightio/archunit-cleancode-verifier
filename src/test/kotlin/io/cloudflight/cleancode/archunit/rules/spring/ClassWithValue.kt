package io.cloudflight.cleancode.archunit.rules.spring

import org.springframework.beans.factory.annotation.Value

class ClassWithValue(@Value("") foo: String)

class ClassWithValueOnMethod {
    fun foo(@Value("") foo: String) {}
}

class ClassWithValueOnField {
    @Value("")
    lateinit var foo: String
}