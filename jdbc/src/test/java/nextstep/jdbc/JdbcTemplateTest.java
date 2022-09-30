package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;
    private JdbcTemplate template;

    @BeforeEach
    void setUp() throws SQLException {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.template = new JdbcTemplate(this.dataSource);
        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
    }

    @Test
    @DisplayName("PreparedStatementSetter에 지정된 콜백을 실행하고 자원을 닫는다.")
    public void testPreparedStatementSetterSucceeds() throws Exception {
        // given
        final String sql = "UPDATE FOO SET NAME=? WHERE ID = 1";
        final String name = "Gary";
        int expectedRowsUpdated = 1;
        given(this.preparedStatement.executeUpdate()).willReturn(expectedRowsUpdated);

        // when
        PreparedStatementSetter pss = ps -> ps.setString(1, name);

        // then
        int actualRowsUpdated = new JdbcTemplate(this.dataSource).update(sql, pss);
        assertThat(expectedRowsUpdated).isEqualTo(actualRowsUpdated);
        verify(this.preparedStatement).setString(1, name);
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    @Test
    @DisplayName("예외 발생시 자원을 닫는다.")
    public void testPreparedStatementSetterFails() throws Exception {
        // given
        final String sql = "UPDATE FOO SET NAME=? WHERE ID = 1";
        final String name = "Gary";
        SQLException sqlException = new SQLException();
        given(this.preparedStatement.executeUpdate()).willThrow(sqlException);

        // when
        PreparedStatementSetter pss = ps -> ps.setString(1, name);

        // then
        assertThatExceptionOfType(DataAccessException.class).isThrownBy(() ->
                        new JdbcTemplate(this.dataSource).update(sql, pss))
                .withCause(sqlException);
        verify(this.preparedStatement).setString(1, name);
        verify(this.preparedStatement).close();
        verify(this.connection, atLeastOnce()).close();
    }
}
