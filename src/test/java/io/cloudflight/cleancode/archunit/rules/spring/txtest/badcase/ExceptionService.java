package io.cloudflight.cleancode.archunit.rules.spring.txtest.badcase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExceptionService {
    @Transactional
    public void foo() throws IllegalAccessException {
    }
}
