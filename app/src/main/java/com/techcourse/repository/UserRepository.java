package com.techcourse.repository;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void insert(final Connection connection, final User user);

    void update(final Connection connection, final User user);

    List<User> findAll(final Connection connection);

    Optional<User> findById(final Connection connection, final Long id);

    Optional<User> findByAccount(final Connection connection, final String account);
}
