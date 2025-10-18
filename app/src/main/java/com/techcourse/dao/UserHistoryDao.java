package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public void log(final UserHistory userHistory) {
        final var sql = """
                insert into user_history
                    (user_id, account, password, email, created_at, created_by) 
                values (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }

    public void log(final Connection connection,
                    final UserHistory userHistory
    ) {
        final var sql = """
                insert into user_history
                    (user_id, account, password, email, created_at, created_by) 
                values (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                connection,
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }
}
