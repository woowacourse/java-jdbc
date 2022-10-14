package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JdbTemplate 은 ")
class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final Connection mockedConnection = mock(Connection.class);
    private final PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
    }

    @DisplayName("레코드 하나를 조회할 수 있어야 한다.")
    @Test
    void query() throws SQLException {
        final ResultSet mockedResultSet = mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        final String sqlFormat = "select * from user where id=?";

        assertThatThrownBy(() -> jdbcTemplate.query(sqlFormat, (resultSet) -> resultSet.getLong("id"), 1L))
                .isInstanceOf(IllegalCallerException.class);
        verify(mockedPreparedStatement).close();
        verify(mockedResultSet).close();
    }

    @DisplayName("레코드 여러개를 조회할 수 있어야 한다.")
    @Test
    void queryForList() throws SQLException {
        final ResultSet mockedResultSet = mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        final String sqlFormat = "select * from user where id>=?";

        assertThatThrownBy(() -> jdbcTemplate.queryForList(sqlFormat, (resultSet) -> resultSet.getLong("id"), 1L))
                .isInstanceOf(IllegalCallerException.class);

        verify(mockedPreparedStatement).close();
        verify(mockedResultSet).close();
    }

    @DisplayName("레코드 하나를 추가할 수 있어야 한다.")
    @Test
    void update() throws SQLException {
        final ResultSet mockedResultSet = mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        final String sqlFormat = "insert into users (account) values (?)";
        jdbcTemplate.update(sqlFormat, "account");

        verify(mockedPreparedStatement).close();
    }
}
