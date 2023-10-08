package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserHistoryDaoTest {

    private static final String SELECT_USER_HISTORY_SQL = "select * from user_history where user_id = ?";
    private static final String DELETE_USER_HISTORY_SQL = "delete from user_history where user_id = ?";

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;
    private User gugu;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        gugu = new User(
                1L,
                "gugu",
                "1q2w3e4r!",
                "test@email.com"
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update(DELETE_USER_HISTORY_SQL, gugu.getId());
    }

    @Test
    void log() {
        // given
        UserHistory expected = new UserHistory(gugu, "emil");

        // when
        userHistoryDao.log(expected);

        // then
        Optional<UserHistory> optionalUserHistory = jdbcTemplate.queryForObject(
                SELECT_USER_HISTORY_SQL,
                (resultSet, rowNumber) -> new UserHistory(
                        resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getString("created_by")
                ),
                gugu.getId()
        );

        assertThat(optionalUserHistory).isPresent()
                .hasValueSatisfying(
                        actual -> {
                            assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
                            assertThat(actual.getAccount()).isEqualTo(expected.getAccount());
                            assertThat(actual.getPassword()).isEqualTo(expected.getPassword());
                            assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
                            assertThat(actual.getCreateBy()).isEqualTo(expected.getCreateBy());
                        }
                );
    }

}
