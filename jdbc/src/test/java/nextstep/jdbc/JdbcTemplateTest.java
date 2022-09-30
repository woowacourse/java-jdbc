package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.fixture.Tester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate template;

    @BeforeEach
    void setUp() throws SQLException {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.resultSet = mock(ResultSet.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.template = new JdbcTemplate(this.dataSource);
        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
    }

    @Test
    @DisplayName("PreparedStatementSetter에 지정된 콜백을 실행하고 자원을 닫는다.")
    void testPreparedStatementSetterSucceeds() throws Exception {
        // given
        final var sql = "UPDATE member SET name=? WHERE id = 1";
        final var name = "awesomeo";
        var expectedRowsUpdated = 1;
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
    void testPreparedStatementSetterFails() throws Exception {
        // given
        final var sql = "UPDATE member SET name=? WHERE id = 1";
        final var name = "Gary";
        var sqlException = new SQLException();
        given(this.preparedStatement.executeUpdate()).willThrow(sqlException);

        // when
        PreparedStatementSetter pss = ps -> ps.setString(1, name);

        // then
        assertThatExceptionOfType(DataAccessException.class).isThrownBy(() ->
                        new JdbcTemplate(this.dataSource).update(sql, pss))
                .withCause(sqlException);
        verify(this.preparedStatement).setString(1, name);
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    @Test
    @DisplayName("PreparedStatementSetter와 RowMapper를 입력받아 단일 쿼리 결과 객체를 리턴한다")
    void testQueryForObjectSucceeds() throws SQLException {
        // given
        final var sql = "SELECT id, name FROM member WHERE id=?";
        var id = 1L;

        given(this.resultSet.next()).willReturn(true);
        given(this.resultSet.getLong(1)).willReturn(1L);
        given(this.resultSet.getString(2)).willReturn("awesomeo");

        PreparedStatementSetter pss = ps -> ps.setLong(1, id);
        RowMapper<Tester> rowMapper = (rs, rowNum) -> new Tester(rs.getLong(1), rs.getString(2));

        // when
        final var member = template.queryForObject(sql, rowMapper, pss).get();

        // then
        assertThat(member).isEqualTo(new Tester(1L, "awesomeo"));

        verify(this.preparedStatement).setLong(1, id);
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }
}
