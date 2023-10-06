package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.SizeException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection mockConnection = mock();
    private DataSource mockDataSource = mock();
    private PreparedStatement mockPreparedStatement = mock();
    private ResultSet mockResultSet = mock();

    @BeforeEach
    void setUp() throws SQLException {
        jdbcTemplate = new JdbcTemplate(mockDataSource);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Nested
    class QueryForObject {

        @Test
        void throwExceptionWhenResultSetIsEmpty() throws SQLException {
            //given
            String sql = "select * from users where id = ?";
            Object[] params = {1L};

            //when
            when(mockResultSet.next()).thenReturn(false);

            //expect
            assertThatThrownBy(
                    () -> jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Integer(1), params))
                    .isInstanceOf(SizeException.class);
        }

        @Test
        void throwExceptionWhenResultSetHasSizeMoreThanOne() throws SQLException {
            //given
            String sql = "select * from users where id = ?";
            Object[] params = {1L};

            //when
            when(mockResultSet.next()).thenReturn(true, true, false);

            //then
            assertThatThrownBy(
                    () -> jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Integer(1), params))
                    .isInstanceOf(DataAccessException.class);
        }

        @Test
        void testCloseEveryClosable() throws Exception {
            //given
            String sql = "select * from users where id = ?";
            Object[] params = {1L};

            //when
            when(mockResultSet.next()).thenReturn(true, false);
            jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Integer(1), params);

            //then
            verify(mockResultSet).close();
            verify(mockPreparedStatement).close();
            verify(mockConnection).close();
        }

    }

    @Nested
    class Update {

        @Test
        void testCloseEveryClosable() throws Exception {
            //given
            String sql = "update users set name = ? where id = ?";
            Object[] params = {"test", 1L};

            //when
            jdbcTemplate.update(sql, params);

            //then
            verify(mockPreparedStatement).close();
            verify(mockConnection).close();
        }
    }

}
