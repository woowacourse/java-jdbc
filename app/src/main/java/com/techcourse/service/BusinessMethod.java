package com.techcourse.service;

@FunctionalInterface
public interface BusinessMethod<T, R> {

    R method(T t) throws Exception;
}
