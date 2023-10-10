package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.TransactionTemplate;

public class UserHistoryDaoWithTransaction extends UserHistoryDao {

    private static final RowMapper<UserHistory> USER_HISTORY_ROW_MAPPER = rs -> new UserHistory(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("created_by"));

    private final TransactionTemplate transactionTemplate;

    public UserHistoryDaoWithTransaction(final DataSource dataSource) {
        super(dataSource);
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    public UserHistoryDaoWithTransaction(final TransactionTemplate transactionTemplate) {
        super(transactionTemplate);
        this.transactionTemplate = transactionTemplate;
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        transactionTemplate.update(connection, sql, userHistory.getUserId(), userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }

    public List<UserHistory> findByUserId(final Connection connection, final Long userId) {
        final var sql = "select * from user_history where user_id = ?";
        return transactionTemplate.query(connection, sql, USER_HISTORY_ROW_MAPPER, userId);
    }
}
