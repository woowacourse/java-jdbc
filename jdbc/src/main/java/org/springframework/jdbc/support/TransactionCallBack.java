package org.springframework.jdbc.support;

@FunctionalInterface
public interface TransactionCallBack {

    void callbackInTransaction();
}
