package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        final DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        this.userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        this.connection = dataSource.getConnection();
    }

    @Test
    void savingUserHistory() {
        assertThatCode(() -> userHistoryDao.log(connection, new UserHistory(1L, 1L, "account", "password", "email", "now")))
                .doesNotThrowAnyException();
    }
}
