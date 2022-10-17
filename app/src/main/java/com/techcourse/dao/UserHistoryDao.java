package com.techcourse.dao;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.techcourse.domain.UserHistory;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.callback.StatementCallback;

public class UserHistoryDao {

	private final JdbcTemplate jdbcTemplate;

	public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void log(final UserHistory userHistory) {
		final var sql = "insert into user_history "
            + "(user_id, account, password, email, created_at, created_by) "
            + "values (?, ?, ?, ?, ?, ?)";
		StatementCallback statementCallback = pstmt -> {
		    pstmt.setLong(1, userHistory.getUserId());
		    pstmt.setString(2, userHistory.getAccount());
		    pstmt.setString(3, userHistory.getPassword());
		    pstmt.setString(4, userHistory.getEmail());
			LocalDateTime createdAt = userHistory.getCreatedAt();
			pstmt.setTimestamp(5, Timestamp.valueOf(createdAt));
		    pstmt.setString(6, userHistory.getCreateBy());
        };
		jdbcTemplate.update(sql, statementCallback);
	}
}
