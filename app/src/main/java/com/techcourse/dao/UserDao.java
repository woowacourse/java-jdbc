package com.techcourse.dao;

import com.techcourse.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Long insert(final User user);

    Long update(final User user);

    List<User> findAll();

    Optional<User> findById(final Long id);

    Optional<User> findByAccount(final String account);

}
