package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.UserServiceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.lang.reflect.Proxy;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionalUserServiceTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserService userService;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:transactional_user_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        userService = UserServiceConfig.create(dataSource);
        userService.save(new User("gugu", "password", "gugu@example.com"));
    }

    @AfterEach
    void assertResourceWasReleased() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void productionFactoryAppliesTheAnnotationProxyAndCommitsBothChanges() {
        assertThat(Proxy.isProxyClass(userService.getClass())).isTrue();

        userService.changePassword(1L, "newPassword", "gugu");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("newPassword");
        final var passwords = jdbcTemplate.query(
                "select password from user_history where user_id = ? and created_by = ?",
                rs -> rs.getString("password"), 1L, "gugu");
        assertThat(passwords).containsExactly("newPassword");
    }

    @Test
    void annotatedMethodRollsBackOnSqlFailureAndCanRunAgain() {
        assertThatThrownBy(() -> userService.changePassword(1L, "failedPassword", null))
                .isInstanceOf(DataAccessException.class);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", rs -> rs.getLong(1));
        assertThat(historyCount).isZero();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        userService.changePassword(1L, "newPassword", "gugu");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("newPassword");
    }

    @Test
    void preservesTheOriginalBusinessExceptionThroughTheProxy() {
        assertThatThrownBy(() -> userService.changePassword(99L, "newPassword", "gugu"))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과를 찾을 수 없습니다.");
    }
}
