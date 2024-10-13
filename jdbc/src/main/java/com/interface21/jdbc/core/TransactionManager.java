package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;

public final class TransactionManager {

    private TransactionManager() {
    }

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    public static <T> T transactionBegin(DataSource dataSource, LogicExecutor<T> businessLogic) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            return execute(connection, businessLogic);
        } catch (SQLException e) {
            log.error("DataSource로부터 Connection을 얻지 못했습니다. 예외 메세지: {}", e.getMessage(), e);
            throw new DataAccessException("데이터베이스 연결을 할 수 없습니다. 원인: " + e.getMessage(), e);
        }
    }

    private static <T> T execute(Connection connection, LogicExecutor<T> businessLogic) throws SQLException {
        try {
            T result = businessLogic.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            connection.rollback();
            log.error("트랜잭션 작업 중 예외 발생으로 인한 롤백: {}", e.getMessage(), e);
            throw new DataAccessException("데이터베이스 작업 중 예외 발생", e);
        }
    }
}
