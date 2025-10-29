package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * 주어진 작업을 트랜잭션 내에서 실행해주는 클래스.
 * 반복되는 try-catch-finally 블록 없이, 실행할 로직만 TransactionalTask 함수형 인터페이스로 전달하면 된다.
 */
public abstract class TransactionExecutionManager {

    private TransactionExecutionManager() {}

    /**
     * 넘겨준 dataSource를 사용하여 Connection을 얻는다.
     * 이후 트랜잭션을 실행하고, 주어진 TransactionalTask를 실행한다.
     * 트랜잭션이 정상적으로 종료되면 commit()을 실행하고 예외가 발생하면 rollback()을 실행한다.
     *
     * @param dataSource 트랜잭션을 사용할 DataSource
     * @param task 트랜잭션 내에서 실행할 로직
     */
    public static void executeInTransaction(DataSource dataSource, TransactionalTask task) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            task.execute();
            connection.commit();
        } catch (Exception exception) {
            try {
                connection.rollback();
            } catch (SQLException sqlException) {
                throw new DataAccessException("DB 롤백 중 오류가 발생했습니다.", sqlException);
            }
            throw new DataAccessException("트랜잭션 처리 중 오류가 발생했습니다.", exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
