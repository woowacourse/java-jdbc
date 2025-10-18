package com.techcourse.service;

import com.interface21.jdbc.InvalidResultSetException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.dao.UserDao;

import java.util.NoSuchElementException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
    }

    public User findById(final long id) {
        return transactionTemplate.returnInTransaction(
                connection -> userDao.findById(id, connection)
        );
    }

    public User findByAccount(final String account) {
        return transactionTemplate.returnInTransaction(
                connection -> {
                    try {
                        return userDao.findByAccount(account, connection);
                    } catch (final InvalidResultSetException e) {
                        throw new NoSuchElementException("해당하는 계정의 회원을 찾을 수 없습니다");
                    }
                }
        );
    }

    public void insert(final User user) {
        transactionTemplate.doInTransaction(
                connection -> userDao.insert(user, connection)
        );
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.doInTransaction(
                connection -> {
                    final var user = findById(id);
                    user.changePassword(newPassword);
                    userDao.update(user, connection);
                    userHistoryDao.log(new UserHistory(user, createBy), connection);
                }
        );
    }
}
