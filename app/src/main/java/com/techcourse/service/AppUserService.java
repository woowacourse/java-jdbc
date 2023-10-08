package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.JdbcUserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.repository.UserRepository;

public class AppUserService implements UserService {

    private final UserRepository userRepository;
    private final JdbcUserHistoryDao jdbcUserHistoryDao;

    public AppUserService(final UserDao userDao, final JdbcUserHistoryDao jdbcUserHistoryDao) {
        this.userRepository = new UserRepository(userDao);
        this.jdbcUserHistoryDao = jdbcUserHistoryDao;
    }

    @Override
    public User findById(final long id) {
        return userRepository.findById(id);
    }

    @Override
    public void insert(final User user) {
        userRepository.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userRepository.update(user);
        jdbcUserHistoryDao.log(new UserHistory(user, createBy));
    }

}
