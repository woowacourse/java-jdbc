package com.techcourse.dao;

import com.techcourse.domain.User;

import java.util.List;

public interface UserDao {

    void insert(final User user);

    void update(final User user);

    List<User> findAll();

    User findById(final Long id);

    User findByAccount(final String account);

}
