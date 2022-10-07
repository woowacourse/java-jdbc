package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class FindExecutorTest {

    @Test
    void List를_반환하는지_확인한다() throws SQLException {
        // given
        class TestUser {

            private final String name;

            TestUser(final String name) {
                this.name = name;
            }
        }

        PreparedStatementStarter pss = mock(PreparedStatementStarter.class);
        ResultSet rs = mock(ResultSet.class);
        when(pss.executeQuery()).thenReturn(rs);

        RowMapper<TestUser> testUserRowMapper = (resultSet) -> List.of(new TestUser("eden"));
        FindExecutor<TestUser> testUserFindExecutor = new FindExecutor<>(testUserRowMapper);

        // when
        List<TestUser> testUsers = testUserFindExecutor.executePreparedStatement(pss);

        // then
        assertThat(testUsers).hasSize(1);
    }
}
