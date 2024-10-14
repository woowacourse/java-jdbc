package com.techcourse.support;

@FunctionalInterface
public interface CheckedExceptionExecutor<R, T extends Throwable> {
    R execute() throws T;
}