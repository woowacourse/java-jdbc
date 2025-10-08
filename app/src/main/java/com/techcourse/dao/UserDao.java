package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementParameter;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementSpecification;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void insert(final User user) {
        PreparedStatementSpecification specification = createPreparedStatementSpecification(
                "insert into users (account, password, email) values (?, ?, ?)",
                List.of(
                        new PreparedStatementParameter(1, user.getAccount()),
                        new PreparedStatementParameter(2, user.getPassword()),
                        new PreparedStatementParameter(3, user.getEmail())
                )
        );
        jdbcTemplate.execute(specification);
    }

    public void update(final User user) {
        PreparedStatementSpecification specification = createPreparedStatementSpecification(
                "update users set account = ?, password = ?, email = ? where id = ?",
                List.of(
                        new PreparedStatementParameter(1, user.getAccount()),
                        new PreparedStatementParameter(2, user.getPassword()),
                        new PreparedStatementParameter(3, user.getEmail()),
                        new PreparedStatementParameter(4, user.getId())
                )
        );
        jdbcTemplate.execute(specification);
    }

    public List<User> findAll() {
        return jdbcTemplate.findAll(
                createPreparedStatementSpecification("select id, account, password, email from users", List.of()),
                USER_ROW_MAPPER
        );
    }

    public User findById(final Long id) {
        PreparedStatementSpecification specification = createPreparedStatementSpecification(
                "select id, account, password, email from users where id = ?",
                List.of(new PreparedStatementParameter(1, id))
        );
        return jdbcTemplate.findOne(specification, USER_ROW_MAPPER)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public User findByAccount(final String account) {
        PreparedStatementSpecification specification = createPreparedStatementSpecification(
                "select id, account, password, email from users where account = ?",
                List.of(new PreparedStatementParameter(1, account))
        );
        return jdbcTemplate.findOne(specification, USER_ROW_MAPPER)
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
