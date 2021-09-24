package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.jdbc.exception.DataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    private static final RowMapper<User> TEST_USER_ROW_MAPPER =
            resultSet -> new User(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email")
            );

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.dataSource = mock(DataSource.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(preparedStatement.executeQuery(anyString())).willReturn(resultSet);
        given(preparedStatement.getConnection()).willReturn(connection);
    }

    @Test
    @DisplayName("insert 쿼리 실행")
    void insert() throws SQLException {
        // given
        String sql = "insert into users (name, email) values (?, ?)";
        String name = "air";
        String email = "air.jnuseo@gmail.com";

        // when
        jdbcTemplate.update(sql, name, email);

        // then
        verify(preparedStatement).setObject(1, name);
        verify(preparedStatement).setObject(2, email);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("update 쿼리 실행")
    void update() throws SQLException {
        // given
        String sql = "update users set name = ?, email = ? where id = ?";
        String name = "air";
        String email = "air.jnuseo@gmail.com";
        Long id = 1L;

        // when
        jdbcTemplate.update(sql, name, email, id);

        // then
        verify(preparedStatement).setObject(1, name);
        verify(preparedStatement).setObject(2, email);
        verify(preparedStatement).setObject(3, id);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("delete 쿼리 실행")
    void delete() throws SQLException {
        // given
        String sql = "delete from users where id = ?";
        Long id = 1L;

        // when
        jdbcTemplate.update(sql, id);

        // then
        verify(preparedStatement).setObject(1, id);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("select all 쿼리 실행")
    void selectAll() throws SQLException {
        // given
        String sql = "select * from users";
        given(resultSet.next()).willReturn(true, true, true, false);

        // when
        List<User> result = jdbcTemplate.query(sql, TEST_USER_ROW_MAPPER);

        // then
        assertThat(result).hasSize(3);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("select where 쿼리 실행. 반환값이 2개 이상인 경우")
    void selectReturnMoreThanTwo() throws SQLException {
        // given
        String sql = "select * from users where name = ?";
        String name = "air";
        given(resultSet.next()).willReturn(true, true, false);

        // when
        List<User> result = jdbcTemplate.query(sql, TEST_USER_ROW_MAPPER, name);

        // then
        assertThat(result).hasSize(2);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("select where 쿼리 실행. 반환값이 1개인 경우")
    void selectOne() throws SQLException {
        // given
        String sql = "select * from users where email = ?";
        String email = "air.junseo@gmail.com";
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("email")).willReturn(email);

        // when
        User user = jdbcTemplate.queryForObject(sql, TEST_USER_ROW_MAPPER, email);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        verify(preparedStatement).setObject(1, email);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("select where 쿼리 실행. 반환값이 없는 경우")
    void selectButNoResult() throws SQLException {
        // given
        String sql = "select * from users where email = ?";
        String email = "air.junseo@gmail.com";
        given(resultSet.next()).willReturn(false);

        // when
        // then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_USER_ROW_MAPPER, email))
                .isInstanceOf(DataAccessException.class);

        verify(preparedStatement).setObject(1, email);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(resultSet).close();
        verify(connection).close();
    }
}
