package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class BusinessService {

    private final DataSource dataSource;

    protected BusinessService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected <R> R transaction(final TransactionalMethod<R> function) {
        try (final var connection = dataSource.getConnection()) {
            // 트랜잭션 시작
            connection.setAutoCommit(false);
            try {
                // 비즈니스 로직 처리
                final R result = function.method(connection);
                // 트랜잭션 커밋
                connection.commit();

                return result;
            } catch (Exception e) {
                // 트랜잭션 롤백
                // 로직 처리 중에 예외가 발생하면 원자성을 보장하기 위해 롤백한다.
                connection.rollback(); // try-catch로 한 번 더 감싸야 하지만 예시니까 생략
                throw new DataAccessException(e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
