package com.techcourse.support.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import java.sql.SQLException;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
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

    private record Product(long id, String name, int price, String description) {
    }
}
