package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id로 사용자를 찾을 수 없습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        User user = findById(id);
        user.changePassword(newPassword);

        try {
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            transactionManager.commit(status);

        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
