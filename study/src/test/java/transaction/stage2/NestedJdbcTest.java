package transaction.stage2;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NestedJdbcTest {

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate outer;
    private TransactionTemplate nested;

    @BeforeEach
    void setUp() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:nested-learning;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("create table if not exists learning_events (name varchar(100))");
        jdbcTemplate.update("delete from learning_events");

        final var transactionManager = new DataSourceTransactionManager(dataSource);
        outer = new TransactionTemplate(transactionManager);
        nested = new TransactionTemplate(transactionManager);
        nested.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
    }

    @Test
    void nestedFailureRollsBackToSavepointAndOuterCanCommit() {
        outer.executeWithoutResult(outerStatus -> {
            jdbcTemplate.update("insert into learning_events values (?)", "outer-before");

            // 내부 예외를 여기서 확인하고 처리하므로 외부 작업을 계속할 수 있다.
            assertThatThrownBy(() -> nested.executeWithoutResult(nestedStatus -> {
                assertThat(nestedStatus.hasSavepoint()).isTrue();
                jdbcTemplate.update("insert into learning_events values (?)", "nested");
                throw new IllegalStateException("nested failure");
            })).isInstanceOf(IllegalStateException.class).hasMessage("nested failure");

            jdbcTemplate.update("insert into learning_events values (?)", "outer-after");
        });

        assertThat(jdbcTemplate.queryForList("select name from learning_events", String.class))
                .containsExactlyInAnyOrder("outer-before", "outer-after");
    }

    @Test
    void outerRollbackAlsoUndoesSuccessfulNestedWork() {
        outer.executeWithoutResult(outerStatus -> {
            jdbcTemplate.update("insert into learning_events values (?)", "outer");
            nested.executeWithoutResult(nestedStatus -> {
                assertThat(nestedStatus.hasSavepoint()).isTrue();
                jdbcTemplate.update("insert into learning_events values (?)", "nested");
            });

            assertThat(jdbcTemplate.queryForObject("select count(*) from learning_events", Integer.class))
                    .isEqualTo(2);
            outerStatus.setRollbackOnly();
        });

        assertThat(jdbcTemplate.queryForList("select name from learning_events", String.class)).isEmpty();
    }
}
