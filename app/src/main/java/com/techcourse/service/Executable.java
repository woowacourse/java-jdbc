package com.techcourse.service;

@FunctionalInterface
public interface Executable<T> {

    T execute();
}
