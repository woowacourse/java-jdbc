package com.techcourse.dao;

import com.techcourse.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    void insert(User user);

    void update(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByAccount(String account);
}
