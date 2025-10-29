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

    public void insert(final User user) {
        CommandSpecification specification = new CommandSpecification(
                PreparedStatementSpecification
                        .builder("insert into users (account, password, email) values (?, ?, ?)")
                        .parameters(
                                List.of(
                                        new PreparedStatementParameter(1, user.getAccount()),
                                        new PreparedStatementParameter(2, user.getPassword()),
                                        new PreparedStatementParameter(3, user.getEmail())
                                )
                        ).build()
        );
        jdbcTemplate.insert(specification);
    }

    public void update(final User user) {
        CommandSpecification specification = new CommandSpecification(
                PreparedStatementSpecification
                        .builder("update users set account = ?, password = ?, email = ? where id = ?")
                        .parameters(
                                List.of(
                                        new PreparedStatementParameter(1, user.getAccount()),
                                        new PreparedStatementParameter(2, user.getPassword()),
                                        new PreparedStatementParameter(3, user.getEmail()),
                                        new PreparedStatementParameter(4, user.getId())
                                )
                        ).build()
        );
        jdbcTemplate.update(specification);
    }

    public List<User> findAll() {
        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                PreparedStatementSpecification
                        .builder("select id, account, password, email from users")
                        .build()
        );
        return jdbcTemplate.findAll(specification);
    }

    public User findById(final Long id) {
        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                PreparedStatementSpecification
                        .builder("select id, account, password, email from users where id = ?")
                        .parameters(List.of(new PreparedStatementParameter(1, id)))
                        .build()
        );
        return jdbcTemplate.findOne(specification)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public User findByAccount(final String account) {

        QuerySpecification<User> specification = new QuerySpecification<>(
                USER_ROW_MAPPER,
                PreparedStatementSpecification
                        .builder("select id, account, password, email from users where account = ?")
                        .parameters(List.of(new PreparedStatementParameter(1, account)))
                        .build()
        );
        return jdbcTemplate.findOne(specification)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}
