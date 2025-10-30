package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy){
        try {
            doInTransaction(connection -> {
                userService.changePassword(id, newPassword, createdBy);
            });
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void doInTransaction(Consumer<Connection> consumer) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (Exception e) {
            log.error("트랜잭션 수행 중 예외 발생");
            handleTransactionException(e, connection);
        } finally {
            closeConnection(connection);
        }
    }

    private void handleTransactionException(Exception e, Connection connection) {
        if(connection != null) {
            try { // 커넥션이 존재할 경우 롤백 시도
                connection.rollback();
                log.info("Rollback 완료");
            } catch (SQLException ex) { // 롤백 도중 예외 발생
                log.error("Rollback 실패", ex);
            }
        }
        throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }

    private void closeConnection(Connection connection) {
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
