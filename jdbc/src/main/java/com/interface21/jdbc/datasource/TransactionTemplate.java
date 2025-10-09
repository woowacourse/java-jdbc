package com.interface21.jdbc.datasource;

/**
 * action을 하나의 트랜잭션으로 관리
 * @see <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/transaction/support/TransactionTemplate.java">Spring TransactionTemplate</a>
 */
public class TransactionTemplate {

    private final DataSourceTransactionManager transactionManager;

    public TransactionTemplate(DataSourceTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void execute(Runnable action) {
        transactionManager.begin();
        try {
            action.run();
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
