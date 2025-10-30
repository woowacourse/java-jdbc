package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.util.function.Supplier;

public class TransactionTemplate {

    private final TransactionManager transactionManager;

    public TransactionTemplate(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // 반환값이 없을 때 사용
    public void execute(Runnable runnable) {
        transactionManager.start();
        try {
            runnable.run();
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException("메서드 실행 중 예외가 발생하여 롤백합니다.", e);
        }
    }

    // 반환값이 있을 때 사용
    public <T> T execute(Supplier<T> task) {
        transactionManager.start();
        try {
            T result = task.get();
            transactionManager.commit();
            return result;
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException("메서드 실행 중 예외가 발생하여 롤백합니다.", e);
        }
    }
}
