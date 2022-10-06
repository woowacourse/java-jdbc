package nextstep.jdbc.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import nextstep.support.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceUtils;

class QueryExecutionTest {

    private DataSource dataSource = DataSourceConfig.getInstance();

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(dataSource, "schema.sql");
        DatabasePopulatorUtils.execute(dataSource, "data.sql");
    }

    @Test
    void executeSelectQuery() throws SQLException {
        // given
        Execution<List<Member>> execution = new QueryExecution<>(
                "select * from member where name = ?",
                (resultSet, rowNum) -> new Member(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age")
                ), new Object[]{"hi"}
        );

        Connection connection = DataSourceUtils.getConnection(dataSource);
        PreparedStatement statement = connection.prepareStatement(execution.getSql());

        // when
        List<Member> members = execution.execute(statement);

        // then
        assertThat(members).hasSize(1);
    }
}
