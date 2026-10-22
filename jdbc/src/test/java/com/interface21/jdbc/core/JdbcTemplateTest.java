package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update는_인자를_순서대로_바인딩하고_변경된_행_수를_반환한다() throws SQLException {
        given(preparedStatement.executeUpdate()).willReturn(1);

        final var updated = jdbcTemplate.update("insert into users (account, password) values (?, ?)", "gugu", "password");

        assertThat(updated).isEqualTo(1);
        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, "gugu");
        inOrder.verify(preparedStatement).setObject(2, "password");
        inOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    void update가_끝나면_PreparedStatement와_Connection을_닫는다() throws SQLException {
        jdbcTemplate.update("delete from users");

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void update_중_SQLException이_나면_DataAccessException으로_바꾸고_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("syntax error");
        given(preparedStatement.executeUpdate()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("wrong sql"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
