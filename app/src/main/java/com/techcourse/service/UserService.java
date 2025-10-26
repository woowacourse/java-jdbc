package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
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
        try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
            try {
                conn.setAutoCommit(false);
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new IllegalStateException("비밀번호 변경 중 오류가 발생했습니다.");
            }

        } catch (SQLException e) {
            throw new IllegalStateException("데이터베이스 연결에 실패했습니다.");
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
