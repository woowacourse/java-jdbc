package nextstep.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.testUtil.TestDataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.ConnectionHolder;
import org.springframework.jdbc.support.TransactionManager;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionManagerTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = TestDataSourceConfig.getInstance();
    }

    @Test
    void 트랜잭션_매니저는_트랜잭션을_시작하면_트랜잭션이_켜진_ConnectionHolder를_반환한다() throws SQLException {
        // when
        TransactionManager.beginTransaction();

        // expected
        final ConnectionHolder connection = TransactionManager.getConnectionHolder(dataSource);
        assertThat(connection)
                .usingRecursiveComparison()
                .comparingOnlyFieldsOfTypes(Boolean.class)
                .isEqualTo(ConnectionHolder.activeTransaction(dataSource.getConnection()));
    }

    @Test
    void 트랜잭션_매니저는_트랜잭션_시작을안하면_트랜잭션이_꺼진_ConnectionHolder를_반환한다() throws SQLException {
        // expected
        final ConnectionHolder connection = TransactionManager.getConnectionHolder(dataSource);
        assertThat(connection)
                .usingRecursiveComparison()
                .comparingOnlyFieldsOfTypes(Boolean.class)
                .isEqualTo(ConnectionHolder.disableTransaction(dataSource.getConnection()));
    }
}
