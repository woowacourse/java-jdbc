package com.interface21.jdbc.core;

public class TransactionHolder {

    private static Transaction transaction;

    public static void setTransaction(Transaction transaction) {
        TransactionHolder.transaction = transaction;
    }

    public static Transaction getTransaction() {
        return transaction;
    }
}
