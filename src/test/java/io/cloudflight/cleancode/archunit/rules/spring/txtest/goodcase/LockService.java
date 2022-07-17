package io.cloudflight.cleancode.archunit.rules.spring.txtest.goodcase;

public interface LockService {
    void runWithLock(String lockName, Runnable task);
}
