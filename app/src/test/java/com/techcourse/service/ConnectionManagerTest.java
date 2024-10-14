package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.core.ConnectionManager;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConnectionManagerTest {

    private ConnectionManager connectionManager;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        this.connectionManager = new ConnectionManager(dataSource);
        this.userDao = new UserDao(new JdbcTemplate(dataSource));
    }

    @DisplayName("파라미터로 전달한 로직을 성공적으로 실행한다.")
    @Test
    void success() {
        // given
        User expected = new User("ever", "password", "ever@woowahan.com");

        // when
        connectionManager.manage(conn -> {
            userDao.insert(expected);
        });

        // then
        User actual = userDao.findById(1L);
        assertThat(actual.getAccount()).isEqualTo(expected.getAccount());
    }

    @DisplayName("Connection 가져올 때 실패할 경우 예외가 발생한다.")
    @Test
    void fail() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        connectionManager = new ConnectionManager(dataSource);

        User expected = new User("ever", "password", "ever@woowahan.com");

        // when & then
        assertThatThrownBy(() -> {
            connectionManager.manage(conn -> {
                userDao.insert(expected);
            });
        }).isInstanceOf(CannotGetJdbcConnectionException.class);
    }
}
