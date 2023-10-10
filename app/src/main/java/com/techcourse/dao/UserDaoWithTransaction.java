package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.TransactionTemplate;

public class UserDaoWithTransaction extends UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final TransactionTemplate transactionTemplate;

    public UserDaoWithTransaction(final DataSource dataSource) {
        super(dataSource);
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    public UserDaoWithTransaction(final TransactionTemplate transactionTemplate) {
        super(transactionTemplate);
        this.transactionTemplate = transactionTemplate;
    }

    public void insert(final Connection connection, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        transactionTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";
        transactionTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(),
                user.getId());
    }

    public List<User> findAll(final Connection connection) {
        final var sql = "select * from users";
        return transactionTemplate.query(connection, sql, USER_ROW_MAPPER);
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return transactionTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final Connection connection, final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return transactionTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, account);
    }
}
