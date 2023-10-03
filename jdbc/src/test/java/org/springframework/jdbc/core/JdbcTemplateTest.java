package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.exception.DataNotFoundException;
import org.springframework.jdbc.core.test_supporter.DataSourceConfig;
import org.springframework.jdbc.core.test_supporter.DatabasePopulatorUtils;
import org.springframework.jdbc.core.test_supporter.User;
import org.springframework.jdbc.core.test_supporter.UserDao;

class JdbcTemplateTest {

    private static final User USER_FIXTURE = new User("hong-sile", "hong", "hong@teco.com");
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    private final UserDao userDao = new UserDao(DataSourceConfig.getInstance());

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    @DisplayName("execute Query로 읽는 쿼리를 실행할 수 있다.")
    void executeQuery() {
        userDao.insert(USER_FIXTURE);
        final String sql = "select id, account, password, email from users where id = ?";
        final Long id = 1L;

        final User actual = jdbcTemplate.executeQuery(sql, (rs) ->
                new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
                )
            , id);

        assertAll(
            () -> assertThat(id)
                .isEqualTo(actual.getId()),
            () -> assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(USER_FIXTURE)
        );
    }

    @Test
    @DisplayName("execute로 쓰는 쿼리를 실행할 수 있다.")
    void execute() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        final Long id = jdbcTemplate.executeUpdate(
            sql,
            USER_FIXTURE.getAccount(), USER_FIXTURE.getPassword(), USER_FIXTURE.getEmail()
        );

        final User actual = userDao.findById(id);

        assertAll(
            () -> assertThat(id)
                .isEqualTo(actual.getId()),
            () -> assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(USER_FIXTURE)
        );
    }

    @Test
    @DisplayName("data가 존재하지 않는경우 Exception을 throw한다.")
    void noDataFoundException() {
        final String sql = "select id, account, password, email from users where id = ?";
        final Long unvalidatedId = Long.MIN_VALUE;

        final ThrowingCallable testTarget = () -> jdbcTemplate.executeQuery(sql, (rs) ->
                new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
                ),
            unvalidatedId
        );

        assertThatThrownBy(testTarget)
            .isInstanceOf(DataNotFoundException.class);
    }
}
