package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

public class UserHistoryDaoTest {
    private static final Long TEST_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final RowMapper<UserHistory> ROW_MAPPER = resultSet -> new UserHistory(
            resultSet.getLong("id"),
            resultSet.getLong("user_id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"),
            resultSet.getString("created_by"));

    @DisplayName("userHistory를 저장할 수 있다.")
    @Test
    void testLog() {
        // given
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        final DataSource dataSource = DataSourceConfig.getInstance();
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final UserHistoryDao userHistoryDao = new UserHistoryDao(dataSource);
        final UserHistory expected = new UserHistory(
                TEST_ID, TEST_USER_ID, "test_account", "test_password", "test_email", "testCreateBy");

        // when
        userHistoryDao.log(expected);

        // then
        final String query = "select * from user_history where id = ?";
        final Optional<UserHistory> optionalUserHistory = jdbcTemplate.queryForObject(query, ROW_MAPPER, TEST_ID);
        assert optionalUserHistory.isPresent();
        final UserHistory actual = optionalUserHistory.get();

        assertThat(actual).isEqualTo(expected);
    }
}
