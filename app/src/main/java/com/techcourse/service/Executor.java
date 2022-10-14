package com.techcourse.service;

@FunctionalInterface
public interface Executor<T> {

    T execute(Object... objects);
}
