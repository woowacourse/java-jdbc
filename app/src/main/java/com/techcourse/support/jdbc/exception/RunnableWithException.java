package com.techcourse.support.jdbc.exception;

@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
