package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

  private UserHistoryDao userHistoryDao;

  @BeforeEach
  void setUp() {
    DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    jdbcTemplate.update("truncate table user_history restart identity");
    userHistoryDao = new UserHistoryDao(jdbcTemplate);
  }

  @Test
  void logStoresAllHistoryFields() throws SQLException {
    final User user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
    final UserHistory userHistory = new UserHistory(user, "gugu");

    userHistoryDao.log(userHistory);

    final String sql =
        "select user_id, account, password, email, created_at, created_by from user_history";
    try (Connection connection = DataSourceConfig.getInstance().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery()) {
      assertThat(resultSet.next()).isTrue();
      assertThat(resultSet.getLong("user_id")).isEqualTo(userHistory.getUserId());
      assertThat(resultSet.getString("account")).isEqualTo(userHistory.getAccount());
      assertThat(resultSet.getString("password")).isEqualTo(userHistory.getPassword());
      assertThat(resultSet.getString("email")).isEqualTo(userHistory.getEmail());
      assertThat(resultSet.getTimestamp("created_at").toLocalDateTime())
          .isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.MILLIS));
      assertThat(resultSet.getString("created_by")).isEqualTo(userHistory.getCreateBy());
      assertThat(resultSet.next()).isFalse();
    }
  }
}
