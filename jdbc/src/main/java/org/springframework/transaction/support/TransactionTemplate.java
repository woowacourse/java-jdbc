package org.springframework.transaction.support;

import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSourceTransactionManager transactionManager;

    public TransactionTemplate(DataSource dataSource) {
        this.transactionManager = new DataSourceTransactionManager(dataSource);
    }

    public <T> T execute(TransactionCallBack<T> action) {
        try {
            transactionManager.startTransaction();
            T result = action.doInTransaction();
            transactionManager.commit();
            return result;
        } catch (RuntimeException | Error e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
