package com.techcourse.service;

@FunctionalInterface
public interface TransactionalDaoExecutor<T> {

    T execute();
}
