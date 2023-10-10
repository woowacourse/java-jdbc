package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDaoWithJdbcTemplate;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.SQLTransactionRollbackException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class UserService {

    private final UserDaoWithJdbcTemplate userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDaoWithJdbcTemplate userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = DataSourceConfig.getInstance();
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        final Optional<User> user = userDao.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("회원을 찾을 수 없음");
        }
        return user.get();
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            connection.commit();
        } catch (RuntimeException | SQLException e) {
            handleTransactionFailure(connection, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void handleTransactionFailure(final Connection connection, final Exception e) {
        try {
            connection.rollback();
            throw new RuntimeException(e);
        } catch (SQLException ex) {
            throw new SQLTransactionRollbackException(ex);
        }
    }
}
