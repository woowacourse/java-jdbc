package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class TransactionManager { // todo: refactoring

    private final DataSource dataSource;

    public TransactionManager() {
        this.dataSource = DataSourceConfig.getInstance();
    }

    public void manage(Consumer<Connection> businessLogic) {
        try (Connection conn = dataSource.getConnection()) {
            try {
                // 트랜잭션 시작
                conn.setAutoCommit(false);

                // 비즈니스 로직 처리
                businessLogic.accept(conn);

                // 트랜잭션 커밋
                conn.commit();
            } catch (SQLException e) {
                // 트랜잭션 롤백
                conn.rollback();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T manage(Function<Connection, T> businessLogic) {
        try (Connection conn = dataSource.getConnection()) {
            try {
                // 트랜잭션 시작
                conn.setAutoCommit(false);

                // 비즈니스 로직 처리
                T result = businessLogic.apply(conn);

                // 트랜잭션 커밋
                conn.commit();

                return result;
            } catch (SQLException e) {
                // 트랜잭션 롤백
                conn.rollback();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        throw new DataAccessException();
    }
}
