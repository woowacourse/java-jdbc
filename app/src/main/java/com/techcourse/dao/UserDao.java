package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public interface UserDao {

    void insert(final User user);

    void update(final User user);

    void update(final Connection conn, final User user);

    List<User> findAll();

    Optional<User> findById(final Long id);

    Optional<User> findByAccount(final String account);

    DataSource getDataSource();
}
