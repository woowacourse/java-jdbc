package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.PreparedStatement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplateBase;
import org.springframework.jdbc.core.error.TableNotFoundSqlRuntimeException;
import org.springframework.jdbc.core.error.exception.ColumnSqlRuntimeException;
import org.springframework.jdbc.core.error.exception.DataConversionSqlRuntimeException;
import org.springframework.jdbc.core.error.exception.MethodNotAllowedSqlRuntimeException;
import org.springframework.jdbc.core.error.exception.SyntaxSqlRuntimeException;
import org.springframework.jdbc.core.mapper.ResultSetObjectMapper;

class SqlRuntimeExceptionTest {

    private JdbcTemplate jdbcTemplate;

    private static final String USER_ACCOUNT_COLUMN = "account";
    private static final String USER_PASSWORD_COLUMN = "password";
    private static final String USER_EMAIL_COLUMN = "email";
    private static final String USER_ID_COLUMN = "id";
    private static final ResultSetObjectMapper<User> USER_OBJECT_MAPPER = resultSet -> {
        final long userId = resultSet.getLong(USER_ID_COLUMN);
        final String userAccount = resultSet.getString(USER_ACCOUNT_COLUMN);
        final String userPassword = resultSet.getString(USER_PASSWORD_COLUMN);
        final String userEmail = resultSet.getString(USER_EMAIL_COLUMN);
        return new User(userId, userAccount, userPassword, userEmail);
    };

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    @Test
    void executeUpdate_select() {
        //given
        final String sql = "select id from users";

        //when
        //then
        assertThatThrownBy(() -> jdbcTemplate.update(sql))
            .isInstanceOf(MethodNotAllowedSqlRuntimeException.ExecuteUpdateSqlRuntimeException.class);
    }

    @Test
    void executeQuery_insert() {
        //given
        final String sql = "insert into users (account, password, email) values ('hi', 'hi', 'hi')";
        final JdbcTemplateBase jdbcTemplateExecutionBase = new JdbcTemplateBase(jdbcTemplate.getDataSource());

        //when
        //then
        assertThatThrownBy(
            () -> jdbcTemplateExecutionBase.executionBaseWithNonReturn(sql, PreparedStatement::executeQuery, true)
        ).isInstanceOf(MethodNotAllowedSqlRuntimeException.ExecuteQuerySqlRuntimeException.class);
    }

    @Test
    void wrong_method_name() {
        //given
        final String sql = "selects id from users";

        //when
        //then
        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject(sql, USER_OBJECT_MAPPER))
            .isInstanceOf(SyntaxSqlRuntimeException.class);
    }

    @Test
    void wrong_table_name() {
        //given
        final String sql = "select id from wrongUser";

        //when
        //then
        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject(sql, USER_OBJECT_MAPPER))
            .isInstanceOf(TableNotFoundSqlRuntimeException.class);
    }

    @Test
    void wrong_column_name() {
        //given
        final String sql = "select nocolumn from users";

        //when
        //then
        assertThatThrownBy(() -> jdbcTemplate.executeQueryForObject(sql, USER_OBJECT_MAPPER))
            .isInstanceOf(ColumnSqlRuntimeException.ColumnNotFoundException.class);
    }

    @Test
    void result_not_exist() {
        try {
            final User user = jdbcTemplate.executeQueryForObject("select id from users", USER_OBJECT_MAPPER);
            assertThat(user).isNull();
        } catch (RuntimeException e) {
            Assertions.fail("결과가 없어도 예외를 던지면 안된다.");
        }
    }

    @Test
    void data_conversion_error() {
        //given
        final String sql = "insert into users (id, account, password, email) values ('hi','hi','hi','hi')";

        //when
        //then
        assertThatThrownBy(() -> jdbcTemplate.update(sql))
            .isInstanceOf(DataConversionSqlRuntimeException.class);
    }

    @Test
    void data_count_mismatch() {
        //given
        final String sql = "insert into users (account, password, email) values ('hi', 3)";

        //when
        //then
        assertThatThrownBy(() -> jdbcTemplate.update(sql))
            .isInstanceOf(ColumnSqlRuntimeException.ColumnCountDoestNotMatchException.class);
    }
}
