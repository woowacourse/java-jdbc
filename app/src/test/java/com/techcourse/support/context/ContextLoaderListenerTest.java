package com.techcourse.support.context;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ContextLoaderListenerTest {

    private Connection databaseAnchor;
    private UserDao userDao;
    private ContextLoaderListener listener;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = DataSourceConfig.createDataSource("jdbc:h2:mem:startup-" + UUID.randomUUID());
        databaseAnchor = dataSource.getConnection();
        userDao = new UserDao(new JdbcTemplate(dataSource));
        listener = new ContextLoaderListener(dataSource, userDao);
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        databaseAnchor.close();
    }

    @Test
    @DisplayName("빈 DB의 스키마를 먼저 초기화한 뒤 기존 기본 계정을 생성한다")
    void createsTheDefaultAccountAfterInitializingTheSchema() {
        listener.contextInitialized(mock(ServletContextEvent.class));

        final var user = userDao.findByAccount("gugu");
        assertThat(user).isNotNull();
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(userDao.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("초기화를 다시 실행해도 기존 계정을 중복 생성하거나 비밀번호를 덮어쓰지 않는다")
    void repeatedInitializationPreservesTheExistingAccount() {
        listener.contextInitialized(mock(ServletContextEvent.class));
        final var user = userDao.findByAccount("gugu");
        user.changePassword("changedPassword");
        userDao.update(user);

        listener.contextInitialized(mock(ServletContextEvent.class));

        final var actual = userDao.findByAccount("gugu");
        assertThat(actual.getId()).isEqualTo(user.getId());
        assertThat(actual.getPassword()).isEqualTo("changedPassword");
        assertThat(userDao.findAll()).hasSize(1);
    }
}
