package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void update() throws SQLException {
        final var sql = "update users set account = 'newAccount'";

        jdbcTemplate.update(sql);

        verify(conn).prepareStatement(sql);
        verify(pstmt).executeUpdate();
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    void queryForObject() throws SQLException {
        final var sql = "select id, account, password, email from users where id = ?";
        final Data expectedObject = new Data(1L);
        final RowMapper<Data> rowMapper = (rs) -> expectedObject;

        when(resultSet.next()).thenReturn(true, false);

        final Data actualObject = jdbcTemplate.queryForObject(sql, rowMapper);

        verify(conn).prepareStatement(sql);
        verify(pstmt).executeQuery();
        assertThat(actualObject).isEqualTo(expectedObject);
        verify(resultSet).close();
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    void query() throws SQLException {
        final var sql = "select id, account, password, email from users";
        final List<Data> expectedObjects = List.of(new Data(1L), new Data(2L), new Data(3L));
        final RowMapper<Data> rowMapper = listRowMapper(expectedObjects);

        when(resultSet.next()).thenReturn(true, true, true, false);

        final List<Data> actualObjects = jdbcTemplate.query(sql, rowMapper);

        verify(conn).prepareStatement(sql);
        verify(resultSet).close();
        verify(pstmt).close();
        verify(conn).close();
        assertThat(actualObjects).isEqualTo(expectedObjects);
    }

    private <T> RowMapper<T> listRowMapper(final List<T> list) {
        return new RowMapper<>() {
            private int index = 0;

            @Override
            public T mapRow(final ResultSet rs) {
                if (index < list.size()) {
                    return list.get(index++);
                }
                return null;
            }
        };
    }

    private static class Data {
        private final long id;

        public Data(final long id) {
            this.id = id;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Data data = (Data) o;
            return id == data.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
