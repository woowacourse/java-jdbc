package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatNoException;

import nextstep.jdbc.execution.Execution;
import nextstep.jdbc.execution.UpdateExecution;
import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcConnectorTest {

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance(), "schema.sql");
    }

    @Test
    void execute() {
        // given
        JdbcConnector connector = new JdbcConnector(DataSourceConfig.getInstance());
        String sql = "insert into member (name, age) values (?, ?)";
        Object[] arguments = new Object[]{"hello", 123};

        // when
        Execution<Integer> updateExecution = new UpdateExecution(sql, arguments);

        // then
        assertThatNoException().isThrownBy(
                () -> connector.execute(updateExecution)
        );
    }
}
