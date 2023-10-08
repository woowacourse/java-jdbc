package org.springframework.jdbc.core;

@FunctionalInterface
public interface StatementExecution<T, R> {

    R apply(T t) throws Exception;

}
