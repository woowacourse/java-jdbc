package com.interface21.transaction;

import com.interface21.transaction.support.TransactionHolder;

public interface PlatformTransactionManager {

    TransactionHolder startTransaction() throws TransactionException;

    void commit(TransactionHolder status) throws TransactionException;

    void rollback(TransactionHolder status) throws TransactionException;
}
