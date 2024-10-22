package com.interface21.jdbc.support;

@FunctionalInterface
public interface CheckedExceptionExecutor<R, T extends Throwable> {
    R execute() throws T;
}