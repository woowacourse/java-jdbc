package com.interface21.transaction.support;

public class JdbcTransactionTemplate {

    private final JdbcTransactionManager transactionManager;

    public JdbcTransactionTemplate(JdbcTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void execute(JdbcNoResultTransactionCallback noResultAction) {
        JdbcTransaction transaction = transactionManager.getTransaction();
        try {
            noResultAction.doInTransaction(transaction);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            transactionManager.flush(transaction);
        }
    }
}
