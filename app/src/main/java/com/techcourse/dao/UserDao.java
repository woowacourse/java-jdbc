package com.techcourse.dao;

import com.techcourse.domain.User;

import java.util.List;

public interface UserDao {

    public void insert(final User user);

    public void update(final User user);

    public List<User> findAll();

    public User findById(final Long id);

    public User findByAccount(final String account);

}
