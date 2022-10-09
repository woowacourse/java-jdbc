package nextstep.jdbc.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceUtils;

class UpdateExecutionTest {

    private DataSource dataSource = DataSourceConfig.getInstance();

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance(), "schema.sql");
    }

    @Test
    void executeUpdateQuery() throws SQLException {
        // given
        Execution<Integer> execution = new UpdateExecution(
                "insert into member (name, age) values (?, ?)",
                "hello", 11);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        PreparedStatement statement = connection.prepareStatement(execution.getSql());

        // when
        Integer count = execution.execute(statement);

        // then
        assertThat(count).isEqualTo(1);
    }
}
