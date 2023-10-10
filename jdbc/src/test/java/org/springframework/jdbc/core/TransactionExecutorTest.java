package org.springframework.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.DataSourceConfig;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionExecutorTest {

    private DataSource dataSource;
    private TestUserDao testUserDao;

    @BeforeEach
    void setUp() throws SQLException {
        this.dataSource = DataSourceConfig.getInstance();
        this.testUserDao = new TestUserDao(dataSource);
        createTable();
    }

    @Test
    void 트랜잭션_수행이_끝나면_커넥션을_닫는다() throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionExecutor transactionExecutor = new TransactionExecutor(dataSource);

        transactionExecutor.execute(() -> {});

        assertThat(connection.isClosed()).isTrue();
    }

    @Test
    void 트랜잭션_중에_예외가_발생하면_모두_롤백되어_원자성을_보장한다() {
        TransactionExecutor transactionExecutor = new TransactionExecutor(dataSource);
        assertThatThrownBy(() -> transactionExecutor.execute(() -> {
            testUserDao.insert("hi");
            testUserDao.fail();
        }));

        transactionExecutor.execute(() -> {
            assertThat(testUserDao.hasUser("hi")).isFalse();
        });
    }

    private void createTable() throws SQLException {
        dataSource.getConnection().prepareStatement(
                "create table if not exists users ( " +
                        "    id bigint auto_increment, " +
                        "    account varchar(100) not null, " +
                        "    primary key(id) " +
                        ");"
        ).executeUpdate();
    }

    private static class TestUserDao {

        private final JdbcTemplate jdbcTemplate;

        public TestUserDao(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        public void insert(String account) {
            String sql = "insert into users (account) values (?)";

            jdbcTemplate.update(sql, account);
        }

        public boolean hasUser(String account) {
            String sql = "select count(*) from users where account = ?";

            int count = jdbcTemplate.queryForObject(sql, resultSet -> resultSet.getInt(1), account);
            return count > 0;
        }

        public void fail() {
            throw new RuntimeException("의도적인 예외 발생");
        }
    }
}
