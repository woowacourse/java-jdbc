package com.techcourse.service;

import com.techcourse.dao.UserDaoWithJdbcTemplate;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserDaoWithJdbcTemplate userDao;
    private final UserHistoryDao userHistoryDao;

    public UserServiceImpl(final UserDaoWithJdbcTemplate userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(final long id) {
        final Optional<User> user = userDao.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("회원을 찾을 수 없음");
        }
        return user.get();
    }

    @Override
    public void insert(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
