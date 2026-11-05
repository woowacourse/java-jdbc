package com.techcourse.support.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.SQLException;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("실제 데이터베이스를 사용하는 JdbcTemplate 통합 테스트")
class JdbcTemplateIntegrationTest {

    private static final RowMapper<Product> PRODUCT_MAPPER = row -> new Product(
            row.getLong("id"), row.getString("name"), row.getInt("price"), row.getString("description"));

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("create table products (id bigint primary key, name varchar(100), "
                + "price int, description varchar(100))");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("SHUTDOWN");
    }

    @Test
    @DisplayName("가변 인자로 도메인 객체를 저장하고 수정한 뒤 조회한다")
    void insertsUpdatesAndQueriesDomainObjectsUsingVarargs() {
        jdbcTemplate.update("insert into products values (?, ?, ?, ?)",
                1L, "keyboard", 1000, "compact");
        assertThat(jdbcTemplate.query("select * from products where id = ?", PRODUCT_MAPPER, 1L))
                .containsExactly(new Product(1L, "keyboard", 1000, "compact"));

        jdbcTemplate.update("update products set price = ? where id = ?", 2000, 1L);

        final var products = jdbcTemplate.query("select * from products where id = ?", PRODUCT_MAPPER, 1L);

        assertThat(products).containsExactly(new Product(1L, "keyboard", 2000, "compact"));
    }

    @Test
    @DisplayName("값 설정 람다로 데이터를 저장하고 조회한다")
    void usesSetterLambdasForWritesAndReads() {
        jdbcTemplate.update("insert into products values (?, ?, ?, ?)", statement -> {
            statement.setLong(1, 2L);
            statement.setString(2, "mouse");
            statement.setInt(3, 500);
            statement.setString(4, "wireless");
        });

        final var products = jdbcTemplate.query("select * from products where price < ?",
                statement -> statement.setInt(1, 1000), PRODUCT_MAPPER);

        assertThat(products).containsExactly(new Product(2L, "mouse", 500, "wireless"));
    }

    @Test
    @DisplayName("null 값과 매개변수 없는 SQL을 처리하고 대상이 없는 수정도 허용한다")
    void supportsNullValuesAndStatementsWithoutParameters() {
        jdbcTemplate.update("insert into products values (?, ?, ?, ?)", 3L, "monitor", 3000, null);

        final var products = jdbcTemplate.query("select * from products", PRODUCT_MAPPER);
        jdbcTemplate.update("delete from products");
        final var remainingProducts = jdbcTemplate.query("select * from products", PRODUCT_MAPPER);

        assertThat(products).containsExactly(new Product(3L, "monitor", 3000, null));
        assertThat(remainingProducts).isEmpty();
        jdbcTemplate.update("update products set price = ? where id = ?", 4000, 3L);
        assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER)).isEmpty();
    }

    @Test
    @DisplayName("데이터베이스 오류를 데이터 접근 예외로 변환하고 이후 조회도 수행한다")
    void translatesDatabaseErrorsWithoutPreventingSubsequentOperations() {
        jdbcTemplate.update("insert into products values (?, ?, ?, ?)", 1L, "keyboard", 1000, null);

        assertThatThrownBy(() -> jdbcTemplate.update("insert into products values (?, ?, ?, ?)",
                1L, "duplicate", 2000, null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
        assertThatThrownBy(() -> jdbcTemplate.query("select * from missing_table", PRODUCT_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER))
                .containsExactly(new Product(1L, "keyboard", 1000, null));
    }

    @Test
    @DisplayName("호출자가 전달한 Connection으로 저장과 조회를 수행하고 직접 커밋한다")
    void commitsUsingTheConnectionOwnedByTheCaller() throws SQLException {
        try (final var connection = jdbcTemplate.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            jdbcTemplate.update(connection, "insert into products values (?, ?, ?, ?)",
                    1L, "keyboard", 1000, null);
            final var products = jdbcTemplate.query(connection, "select * from products", PRODUCT_MAPPER);

            assertThat(products).containsExactly(new Product(1L, "keyboard", 1000, null));
            assertThat(connection.isClosed()).isFalse();
            assertThat(connection.getAutoCommit()).isFalse();
            connection.commit();
        }

        assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER))
                .containsExactly(new Product(1L, "keyboard", 1000, null));
    }

    @Test
    @DisplayName("외부 Connection의 미커밋 변경은 다른 연결에 보이지 않고 호출자가 직접 롤백한다")
    void leavesTransactionCompletionToTheCaller() throws SQLException {
        try (final var connection = jdbcTemplate.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            jdbcTemplate.update(connection, "insert into products values (?, ?, ?, ?)",
                    1L, "keyboard", 1000, null);

            assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER)).isEmpty();
            assertThat(jdbcTemplate.query(connection, "select * from products", PRODUCT_MAPPER)).hasSize(1);

            connection.rollback();
            assertThat(connection.isClosed()).isFalse();
        }

        assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER)).isEmpty();
    }

    @Test
    @DisplayName("Connection을 전달하지 않아도 같은 스레드의 저장과 조회가 하나의 커밋에 참여한다")
    void commitsUsingThreadBoundConnection() throws SQLException {
        final var dataSource = jdbcTemplate.getDataSource();
        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                jdbcTemplate.update("insert into products values (?, ?, ?, ?)", 1L, "keyboard", 1000, null);
                assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER))
                        .containsExactly(new Product(1L, "keyboard", 1000, null));
                assertThat(connection.isClosed()).isFalse();
                connection.commit();
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }

        assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER))
                .containsExactly(new Product(1L, "keyboard", 1000, null));
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("동기화된 Connection의 저장은 별도 연결에 보이지 않고 롤백 후 사라진다")
    void rollsBackUsingThreadBoundConnection() throws SQLException {
        final var dataSource = jdbcTemplate.getDataSource();
        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            try {
                jdbcTemplate.update("insert into products values (?, ?, ?, ?)", 1L, "keyboard", 1000, null);
                try (final var otherConnection = dataSource.getConnection()) {
                    assertThat(jdbcTemplate.query(otherConnection, "select * from products", PRODUCT_MAPPER)).isEmpty();
                }
                assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER)).hasSize(1);
                connection.rollback();
                assertThat(connection.isClosed()).isFalse();
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }

        assertThat(jdbcTemplate.query("select * from products", PRODUCT_MAPPER)).isEmpty();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    private record Product(long id, String name, int price, String description) {
    }
}
