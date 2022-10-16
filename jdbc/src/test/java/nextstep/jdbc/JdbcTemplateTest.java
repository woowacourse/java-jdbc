package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("update 메서드가 종료되면 connection과 preparedStatment가 닫힌다.")
    @Test
    void update_메서드가_정상적으로_종료되면_connection과_preparedStatment가_닫힌다() throws SQLException {
        String sql = "UPDATE users SET account = ?, password = ?, email = ?";
        int update = jdbcTemplate.update(sql, "dummy", "dummypassword", "dummy@aaa.com");

        verify(preparedStatement).close();
    }

    @DisplayName("update 메서드가 성공하면 1을 반환한다.")
    @Test
    void update_메서드가_종료되면() throws SQLException {
        given(preparedStatement.executeUpdate()).willReturn(1);

        String sql = "UPDATE users SET account = ?, password = ?, email = ?";
        int actual = jdbcTemplate.update(sql, "dummy", "dummypassword", "dummy@aaa.com");

        assertThat(actual).isEqualTo(1);
    }

    @DisplayName("query 메서드가 종료되면 connection과 preparedStatment 그리고 resultSet이 닫힌다.")
    @Test
    void qeury_메서드가_종료되면_connection과_preparedStatment_그리고_resultset이_닫힌다() throws SQLException {
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        String sql = "select * from users";
        jdbcTemplate.query(sql, (rs) -> any());

        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @DisplayName("queryForObject 메서드가 종료되면 데이터를 반환한다.")
    @Test
    void queryForObject_메서드가_종료되면_데이터를_반환한다() throws SQLException {
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true);

        String sql = "select * from users where id = ?";
        Object actual = jdbcTemplate.queryForObject(sql, (rs) -> "dummy", 1L);

        assertThat(actual).isNotNull();
    }

    @DisplayName("queryForObject 메서드가 종료될때, 데이터가 1개가 아니면 예외를 발생한다.")
    @Test
    void queryForObject_메서드가_정상적으로_종료되면_Optional_객체를_반환한다() throws SQLException {
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(false);

        String sql = "select * from users where id = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (rs) -> any(), 1L))
                .isInstanceOf(RuntimeException.class);
    }
}
