package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    // todo : Connection 리팩토링 이후 주석 삭제 예정
//    public void insert(final User user) {
//        userDao.insert(user);
//    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            try {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(user);

                userHistoryDao.log(new UserHistory(user, createBy));
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                DataSourceUtils.releaseConnection(conn, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource, conn);
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("비밀번호 변경 실패", e);
        }
    }
}
