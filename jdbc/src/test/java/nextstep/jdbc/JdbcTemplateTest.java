package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcTemplateTests {

    private final DataSource dataSource = mock();
    private final Connection connection = mock();
    private final PreparedStatement preparedStatement = mock();
    private final ResultSet resultSet = mock();
    private JdbcTemplate template = new JdbcTemplate(this.dataSource);

    @BeforeEach
    public void setup() throws Exception {
        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }

    @Test
    void update를_테스트한다() throws Exception {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        template.update(sql, "reo", "1234", "reo@woowahan.com");

        given(this.preparedStatement.executeUpdate()).willReturn(1);

        verify(this.preparedStatement).setObject(1, "reo");
        verify(this.preparedStatement).setObject(2, "1234");
        verify(this.preparedStatement).setObject(3, "reo@woowahan.com");
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    @Test
    void update_중_SQLException이_발생하면_예외를_던진다() throws Exception {
        SQLException sqlException = new SQLException("bad update");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        given(this.preparedStatement.executeUpdate()).willThrow(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> this.template.update(sql))
                .withCause(sqlException);
        verify(this.preparedStatement).close();
        verify(this.connection, atLeastOnce()).close();
    }

    @Test
    void query를_테스트한다() throws Exception {
        String sql = "select id, account, password, email from users";
        List<String> result = template.query(sql, resultSet -> {
            return "";
        });

        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);

        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    @Test
    void query_중_SQLException이_발생하면_예외를_던진다() throws Exception {
        SQLException sqlException = new SQLException("bad query");
        String sql = "select id, account, password, email from users";

        given(this.preparedStatement.executeQuery()).willThrow(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> this.template.query(sql, resultSet -> {return "";}))
                .withCause(sqlException);
        verify(this.preparedStatement).close();
        verify(this.connection, atLeastOnce()).close();
    }
}
