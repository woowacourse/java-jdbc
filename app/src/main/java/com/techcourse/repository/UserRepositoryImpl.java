package com.techcourse.repository;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;

import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final UserDao userDao = new UserDao(DataSourceConfig.getInstance());

    @Override
    public void save(User user) {
        userDao.insert(user);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return Optional.ofNullable(userDao.findByAccount(account));
    }
}
