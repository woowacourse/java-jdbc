package com.techcourse.support;

@FunctionalInterface
public interface TransactionCallBack {

    void doInTransactionWithoutResult();
}
