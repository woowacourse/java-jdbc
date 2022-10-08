package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;

class JdbcTemplateTest {

    private Connection connection;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

/*
    @BeforeEach
    void setUp() throws Exception {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.executeQuery(anyString())).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }
*/

    @BeforeEach
    void setUp() throws SQLException {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.executeQuery(anyString())).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }
}