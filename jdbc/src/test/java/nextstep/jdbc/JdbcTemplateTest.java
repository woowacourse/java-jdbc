package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import javax.sql.DataSource;
import nextstep.jdbc.templates.JdbcTemplate;
import nextstep.jdbc.utils.ResultSetExtractor;
import nextstep.jdbc.utils.TransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        this.dataSource = mock(DataSource.class);
    }

    @Test
    @DisplayName("기존 인서트 메서드 흐름이 Jdbc에 들어있는 지 확인한다. (PrepareStatement)")
    public void insertTest() throws Exception {

        // given
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        final String arg1 = "gugu";
        final String arg2 = "password";
        final String arg3 = "email";

        // when
        jdbcTemplate.update(sql, arg1, arg2, arg3);

        // then
        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);

        verify(preparedStatement).setObject(1, arg1);
        verify(preparedStatement).setObject(2, arg2);
        verify(preparedStatement).setObject(3, arg3);

        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    @DisplayName("기존 select 메서드 흐름이 JdbcTemplate에 들어있는지 확인한다. (Statement)")
    public void select() throws Exception {
        // given
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users";

        // when
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));

        // then
        verify(dataSource).getConnection();
        verify(connection).createStatement();

        verify(connection).close();
        verify(statement).close();
    }

    @Test
    @DisplayName("트랜잭션이 없을 시 Connection이 JdbcTemplate 메서드마다 호출되는 지 확인")
    public void transaction_oneConnection() throws Exception {
        // given
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users";

        // when
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));

        // then
        verify(dataSource, times(3)).getConnection();
        verify(connection, times(3)).close();
    }

    @Test
    @DisplayName("트랜잭션 안에 있을시 Connection이 한 번만 호출되는 지 확인")
    public void inTransaction() throws Exception {
        // given
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users";

        // when
        TransactionManager.startTransaction();
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));
        TransactionManager.commit();

        // then
        verify(dataSource, times(1)).getConnection();
        verify(connection).close();
    }

    @Test
    @DisplayName("롤백 시 롤백이 호출되는 지 확인")
    public void rollback() throws Exception {
        // given
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select * from users";

        // when
        TransactionManager.startTransaction();
        jdbcTemplate.query(sql, mock(ResultSetExtractor.class));
        TransactionManager.rollback();

        // then
        verify(connection).rollback();
        verify(connection).close();
    }
}
