package com.techcourse.dao;

@FunctionalInterface
public interface TransactionSupplier {

    void run();
}
