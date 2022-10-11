package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final PlatformTransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
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
        // 트랜잭션 시작
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 비즈니스 로직 처리
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            // 트랜잭션 커밋
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            // 트랜잭션 롤백
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
