package org.springframework.transaction.support;

@FunctionalInterface
public interface TransactionCallBack<T> {

    T doInTransaction();
}
