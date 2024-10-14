package com.interface21.transaction.support;

import java.sql.SQLException;

public class TransactionalTemplate {
    public void execute(final Transaction transaction, final TransactionCallback transactionCallback) {
        try {
            performTransaction(transaction, transactionCallback);
        } catch (final SQLException e) {
            throw new TransactionalException("트랜잭션 작업중 예외가 발생했습니다.", e);
        }
    }

    private void performTransaction(final Transaction transaction, final TransactionCallback transactionCallback) throws SQLException {
        try {
            transaction.begin();
            transactionCallback.execute(transaction);
            transaction.commit();
        } catch (final SQLException e) {
            transaction.rollback();
            throw new TransactionalException("트랜잭션 작업중 예외가 발생했습니다.", e);
        } finally {
            transaction.close();
        }
    }
}
