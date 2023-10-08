package org.springframework.transaction.support;

@FunctionalInterface
public interface LogicExecutor {

    void run();
}
