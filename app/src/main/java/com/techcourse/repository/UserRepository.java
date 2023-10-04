package com.techcourse.repository;

import com.techcourse.domain.User;
import java.util.List;

public interface UserRepository {

    void insert(final User user);

    void update(final User user);

    List<User> findAll();

    User findById(final Long id);

    User findByAccount(final String account);
}
