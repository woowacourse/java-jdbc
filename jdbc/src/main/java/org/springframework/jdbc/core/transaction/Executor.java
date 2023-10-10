package org.springframework.jdbc.core.transaction;

@FunctionalInterface
public interface Executor {

    Object execute();
}
