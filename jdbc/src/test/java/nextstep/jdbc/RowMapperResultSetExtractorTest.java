package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RowMapperResultSetExtractorTest {

    @DisplayName("ResultSet에 담긴 데이터를 RowMapper 형식에 맞춰 데이터를 반환해줄 수 있다.")
    @Test
    void extractData() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);

        final RowMapper<TestUser> userRowMapper = createUserRowMapper();
        final RowMapperResultSetExtractor<TestUser> rowMapperResultSetExtractor = new RowMapperResultSetExtractor(
                userRowMapper);

        // when
        when(resultSet.next()).thenReturn(true)
                .thenReturn(false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("username")).thenReturn("dwoo");

        final List<TestUser> results = rowMapperResultSetExtractor.extractData(resultSet);

        // then
        final TestUser result = results.get(0);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("dwoo");
    }

    private static RowMapper<TestUser> createUserRowMapper() {
        return (resultSet) -> {
            final long id = resultSet.getLong("id");
            final String username = resultSet.getString("username");
            return new TestUser(id, username);
        };
    }

    static class TestUser {
        private final Long id;
        private final String username;

        public TestUser(final Long id, final String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }
}
