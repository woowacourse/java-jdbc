package com.techcourse.dao;

import java.util.List;

import com.techcourse.domain.User;

public interface UserDao {

    void save(final User user);

    void update(final User user);

    List<User> findAll();

    User findById(final Long id);

    User findByAccount(final String account);
}
