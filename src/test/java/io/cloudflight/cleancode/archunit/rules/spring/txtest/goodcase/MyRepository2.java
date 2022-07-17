package io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyRepository2 extends JpaRepository<String, String> {

    @Override
    default <S extends String> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }
}
