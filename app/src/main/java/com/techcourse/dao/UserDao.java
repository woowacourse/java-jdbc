package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.core.execution.command.CommandSpecification;
import com.interface21.jdbc.core.execution.query.QuerySpecification;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementParameter;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementSpecification;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLSyntaxErrorException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Connection getConnection() {
        return jdbcTemplate.getConnection();
    }

    public void startTransaction(Connection connection) {
        jdbcTemplate.startTransaction(connection);
    }

    public void commitTransaction(Connection connection) {
        jdbcTemplate.commitTransaction(connection);
    }

    public void insertWithTransaction(final User user, final Connection connection) {
        CommandSpecification specification = new CommandSpecification(createPreparedStatementSpecification(
                "insert into users (account, password, email) values (?, ?, ?)",
                List.of(
                        new PreparedStatementParameter(1, user.getAccount()),
                        new PreparedStatementParameter(2, user.getPassword()),
                        new PreparedStatementParameter(3, user.getEmail())
                )
        ));
        jdbcTemplate.insertWithTransaction(specification, connection);
    }

    public void updateWithTransaction(final User user, final Connection connection) {
        CommandSpecification specification = new CommandSpecification(createPreparedStatementSpecification(
                "update users set account = ?, password = ?, email = ? where id = ?",
                List.of(
                        new PreparedStatementParameter(1, user.getAccount()),
                        new PreparedStatementParameter(2, user.getPassword()),
                        new PreparedStatementParameter(3, user.getEmail()),
                        new PreparedStatementParameter(4, user.getId())
                )
        ));
        jdbcTemplate.updateWithTransaction(specification, connection);
    }

    public List<User> findAllWithTransaction(final Connection connection) {
        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                createPreparedStatementSpecification(
                        "select id, account, password, email from users",
                        List.of()
                )
        );
        return jdbcTemplate.findAllWithTransaction(specification, connection);
    }

    public User findByIdWithTransaction(final Long id, final Connection connection) {
        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                createPreparedStatementSpecification(
                        "select id, account, password, email from users where id = ?",
                        List.of(new PreparedStatementParameter(1, id))
                )
        );
        return jdbcTemplate.findOneWithTransaction(specification, connection)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public User findByAccountWithTransaction(final String account, final Connection connection) {

        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                createPreparedStatementSpecification(
                        "select id, account, password, email from users where account = ?",
                        List.of(new PreparedStatementParameter(1, account))
                )
        );
        return jdbcTemplate.findOneWithTransaction(specification, connection)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public void insert(final User user) {
        CommandSpecification specification = new CommandSpecification(createPreparedStatementSpecification(
                "insert into users (account, password, email) values (?, ?, ?)",
                List.of(
                        new PreparedStatementParameter(1, user.getAccount()),
                        new PreparedStatementParameter(2, user.getPassword()),
                        new PreparedStatementParameter(3, user.getEmail())
                )
        ));
        jdbcTemplate.insert(specification);
    }

    public void update(final User user) {
        CommandSpecification specification = new CommandSpecification(createPreparedStatementSpecification(
                "update users set account = ?, password = ?, email = ? where id = ?",
                List.of(
                        new PreparedStatementParameter(1, user.getAccount()),
                        new PreparedStatementParameter(2, user.getPassword()),
                        new PreparedStatementParameter(3, user.getEmail()),
                        new PreparedStatementParameter(4, user.getId())
                )
        ));
        jdbcTemplate.update(specification);
    }

    public List<User> findAll() {
        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                createPreparedStatementSpecification(
                        "select id, account, password, email from users",
                        List.of()
                )
        );
        return jdbcTemplate.findAll(specification);
    }

    public User findById(final Long id) {
        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                createPreparedStatementSpecification(
                        "select id, account, password, email from users where id = ?",
                        List.of(new PreparedStatementParameter(1, id))
                )
        );
        return jdbcTemplate.findOne(specification)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public User findByAccount(final String account) {

        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                createPreparedStatementSpecification(
                        "select id, account, password, email from users where account = ?",
                        List.of(new PreparedStatementParameter(1, account))
                )
        );
        return jdbcTemplate.findOne(specification)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    private PreparedStatementSpecification createPreparedStatementSpecification(
            String sql,
            List<PreparedStatementParameter> parameters
    ) {
        try {
            return PreparedStatementSpecification
                    .builder(sql)
                    .parameters(parameters)
                    .build();
        } catch (SQLSyntaxErrorException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
