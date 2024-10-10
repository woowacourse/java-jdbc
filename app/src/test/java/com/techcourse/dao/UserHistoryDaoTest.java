package com.techcourse.dao;

import com.interface21.jdbc.core.ArgumentPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private static final RowMapper<UserHistory> USER_HISTORY_ROW_MAPPER = (rs, rowNum) ->
            new UserHistory(rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("created_by")
            );

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    @DisplayName("유저의 히스토리 정보를 업데이트 할 수 있다.")
    void update() {
        UserDao userDao = new UserDao(jdbcTemplate);
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        User insertedUser = userDao.findById(1L).get();

        UserHistory userHistory = new UserHistory(insertedUser, "2024.05.10");

        userHistoryDao.insert(userHistory);

        String sql = """
                SELECT id, user_id, account, password, email, created_at, created_by
                FROM user_history
                WHERE id = ?
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(1);

        UserHistory expectedUserHistory = jdbcTemplate.queryForObject(sql, USER_HISTORY_ROW_MAPPER, argumentPreparedStatementSetter);

        assertThat(userHistory.getAccount()).isEqualTo(expectedUserHistory.getAccount());
    }
}
