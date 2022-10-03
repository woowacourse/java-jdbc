package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatNoException;

import nextstep.jdbc.execution.UpdateExecution;
import nextstep.support.DataSourceConfig;
import org.junit.jupiter.api.Test;

class JdbcConnectorTest {

    @Test
    void execute() {
        // given
        JdbcConnector connector = new JdbcConnector(DataSourceConfig.getInstance());
        String sql = "insert into member (name, age) values (?, ?)";
        Object[] arguments = new Object[]{"hello", 123};

        // when
        UpdateExecution updateExecution = new UpdateExecution(sql, arguments);

        // then
        assertThatNoException().isThrownBy(
                () -> connector.execute(updateExecution)
        );
    }
}
