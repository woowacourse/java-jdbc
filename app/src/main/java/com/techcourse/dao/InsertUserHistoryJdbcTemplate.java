package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.util.Map;
import javax.sql.DataSource;

public class InsertUserHistoryJdbcTemplate extends JdbcTemplate<UserHistory> {

    private final DataSource dataSource;

    public InsertUserHistoryJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected String createQuery() {
        return "insert into user_history (user_id, account, password, email, created_at, created_by) " +
                "values (:user_id, :account, :password, :email, :created_at, :created_by)";
    }

    @Override
    protected void setValues(final UserHistory userHistory, final Map<String, Object> params) {
        params.put("user_id", userHistory.getUserId());
        params.put("account", userHistory.getAccount());
        params.put("password", userHistory.getPassword());
        params.put("email", userHistory.getEmail());
        params.put("created_at", userHistory.getCreatedAt());
        params.put("created_by", userHistory.getCreateBy());
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}