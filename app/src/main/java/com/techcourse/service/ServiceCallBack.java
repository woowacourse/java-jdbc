package com.techcourse.service;

@FunctionalInterface
public interface ServiceCallBack<T> {
    T execute();
}
