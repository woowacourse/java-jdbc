package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class RowMapperResultSetExtractorTest {

    @Test
    void extractData() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        given(resultSet.getLong("id")).willReturn(1L);
        given(resultSet.getString("account")).willReturn("hoho");
        given(resultSet.getString("password")).willReturn("password");
        given(resultSet.getString("email")).willReturn("email@email.com");
        given(resultSet.next()).willReturn(true, false);

        RowMapper<JdbcUser> rowMapper = rs -> new JdbcUser(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));

        RowMapperResultSetExtractor<JdbcUser> extractor = new RowMapperResultSetExtractor<>(rowMapper);
        List<JdbcUser> jdbcUsers = extractor.extractData(resultSet);

        assertThat(jdbcUsers).hasSize(1);
    }
}
