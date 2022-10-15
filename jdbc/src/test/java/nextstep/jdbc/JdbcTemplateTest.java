package nextstep.jdbc;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    @Test
    void query() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        JdbcMapper jdbcMapper = mock(JdbcMapper.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("select * from users where id = 1")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(jdbcMapper.mapRow(resultSet)).thenReturn(new TestUser(1L, "bcc0830@naver.com", "1234"));
        JdbcTemplate jdbcTemplate = new TestJdbcTemplate(dataSource);

        // when
        List<TestUser> testUsers = jdbcTemplate.selectQuery("select * from users where id = 1", jdbcMapper);

        // then
        assertAll(
                () -> assertThat(testUsers.size()).isEqualTo(1),
                () -> assertThat(testUsers.get(0)).isEqualTo(new TestUser(1L, "bcc0830@naver.com", "1234"))
        );
    }

    @Test
    void update() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("insert into users (id, email, password) values (2, abc@naver.com, abcd)")).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        JdbcTemplate jdbcTemplate = new TestJdbcTemplate(dataSource);

        // when
        int result = jdbcTemplate.nonSelectQuery("insert into users (id, email, password) values (2, abc@naver.com, abcd)");

        // then
        assertThat(result).isEqualTo(1);
    }
}
