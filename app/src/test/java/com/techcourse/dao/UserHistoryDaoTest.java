package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    private JdbcTemplate jdbcTemplate;

    private final RowMapper<UserHistory> userHistoryRowMapper = (resultSet, rowNum) -> new UserHistory(
            resultSet.getLong("id"),
            resultSet.getLong("user_id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"),
            resultSet.getString("created_by")
    );

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void log() throws SQLException {
        UserHistory userHistory = new UserHistory(1L, 1L, "gugu", "password", "email", "gugu");

        Connection connection = DataSourceConfig.getInstance().getConnection();
        userHistoryDao.log(userHistory,connection);

        List<UserHistory> result = jdbcTemplate.query(
                "select * from user_history",
                userHistoryRowMapper
        );
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(userHistory.getId());
    }

}
