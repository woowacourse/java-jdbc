package com.interface21.jdbc.core;

@FunctionalInterface
public interface TransactionCallBack<T> {

    T doExecute();
}
