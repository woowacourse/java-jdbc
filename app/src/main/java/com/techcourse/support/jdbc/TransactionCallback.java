package com.techcourse.support.jdbc;

@FunctionalInterface
public interface TransactionCallback {

    void execute();
}
