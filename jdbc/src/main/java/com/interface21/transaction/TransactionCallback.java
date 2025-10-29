package com.interface21.transaction;

//https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/transaction/support/TransactionCallback.java
@FunctionalInterface
public interface TransactionCallback<T> {

    T doInTransaction();
}
