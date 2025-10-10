package com.interface21.transaction.support;

@FunctionalInterface
public interface TransactionalMethod<R> {

    R method() throws Exception;
}
