package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.execution.command.CommandSpecification;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementParameter;
import com.interface21.jdbc.core.preparedstatement.PreparedStatementSpecification;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        CommandSpecification commandSpecification = new CommandSpecification(
                createPreparedStatementSpecification(
                        "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)",
                        List.of(
                                new PreparedStatementParameter(1, userHistory.getUserId()),
                                new PreparedStatementParameter(2, userHistory.getAccount()),
                                new PreparedStatementParameter(3, userHistory.getPassword()),
                                new PreparedStatementParameter(4, userHistory.getEmail()),
                                new PreparedStatementParameter(5, userHistory.getCreatedAt()),
                                new PreparedStatementParameter(6, userHistory.getCreateBy())
                        )
                )
        );
        jdbcTemplate.insert(commandSpecification);
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
