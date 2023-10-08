package com.techcourse.repository;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;

public interface UserRepository {

    void insert(final Connection connection, final User user);

    void update(final Connection connection, final User user);

    List<User> findAll(final Connection connection);

    User findById(final Connection connection, final Long id);

    User findByAccount(final Connection connection, final String account);
}
