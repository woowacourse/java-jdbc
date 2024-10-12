package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    TransactionManager transactionManager;
    UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        transactionManager = new TransactionManager(DataSourceConfig.getInstance());
        userDao = new UserDao(new JdbcTemplate());
    }

    @DisplayName("실행을 성공할 경우 커밋한다.")
    @Test
    void should_commitLogic_when_manageSuccessfully() throws SQLException {
        // given
        User expected = new User("ever", "password", "ever@woowahan.com");

        // when
        transactionManager.manage(conn -> {
            userDao.insert(conn, expected);
        });

        // then
        User actual = userDao.findById(DataSourceConfig.getInstance().getConnection(), 1L);
        assertThat(actual.getAccount()).isEqualTo(expected.getAccount());
    }

    @DisplayName("실행을 실패할 경우 롤백한다.")
    @Test
    void should_rollbackLogic_when_manageFail() throws SQLException {
        // given
        User expected = new User("ever", "password", "ever@woowahan.com");

        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        transactionManager = new TransactionManager(dataSource);

        // when
        assertThatThrownBy(() -> {
            transactionManager.manage(conn -> {
                userDao.insert(conn, expected);
            });
        }).isInstanceOf(DataAccessException.class);

        // then
        User actual = userDao.findById(DataSourceConfig.getInstance().getConnection(), 1L);
        assertThat(actual).isNull();
    }
}