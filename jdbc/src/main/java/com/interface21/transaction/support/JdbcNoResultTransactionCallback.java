package com.interface21.transaction.support;

public interface JdbcNoResultTransactionCallback {

    void doInTransaction(JdbcTransaction transaction);
}
