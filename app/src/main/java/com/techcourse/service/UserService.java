package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            doInTransaction(connection -> {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void doInTransaction(Consumer<Connection> consumer) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            consumer.accept(connection);
            connection.commit();
        } catch (Exception e) {
            log.error("비밀번호 변경 중 예외 발생");
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
        if(connection != null){
            try {
                connection.close();
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            } catch (SQLException ignored) {}
        }
    }
}
