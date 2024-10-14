package com.techcourse.dao;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<UserHistory> rowMapper = (resultSet) -> {
        return new UserHistory(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"),
                resultSet.getString("created_by")
        );
    };

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        jdbcTemplate.update("DROP TABLE IF EXISTS userHistory");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @DisplayName("사용자에 대한 기록을 저장한다.")
    @Test
    void log() throws SQLException {
        // given
        User user = new User(1, "gugu", "password", "hkkang@woowahan.com");
        Connection connection = DataSourceConfig.getInstance().getConnection();
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(),
                user.getPassword(),
                user.getEmail());
        String createdBy = "2024-10-04";
        UserHistory userHistory = new UserHistory(user, createdBy);

        // when
        userHistoryDao.log(userHistory);

        // then
        UserHistory result = jdbcTemplate.queryForObject("select * from user_history where user_id = ?", rowMapper, 1);

        assertThat(result.getCreatedBy()).isEqualTo(createdBy);
    }
}
