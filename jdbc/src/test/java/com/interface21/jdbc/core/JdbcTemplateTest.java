package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("create table items (id bigint primary key, name varchar(100))");
            statement.execute("insert into items values (1, 'first'), (2, 'second')");
        }
    }

    @Test
    void updateBindsArgumentsAndReturnsAffectedRows() {
        assertThat(jdbcTemplate.update("update items set name = ? where id = ?", "changed", 2L))
                .isEqualTo(1);
        final var name = jdbcTemplate.queryForObject("select name from items where id = ?",
                (rs, rowNum) -> rs.getString("name"), 2L);
        assertThat(name).isEqualTo("changed");
    }

    @Test
    void queryMapsEveryRowInOrder() {
        assertThat(jdbcTemplate.query("select name from items order by id",
                (rs, rowNum) -> rowNum + ":" + rs.getString("name")))
                .containsExactly("0:first", "1:second");
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsMatch() {
        assertThat(jdbcTemplate.query("select name from items where id = ?",
                (rs, rowNum) -> rs.getString("name"), 999L)).isEmpty();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsMatch() {
        final var name = jdbcTemplate.queryForObject("select name from items where id = ?",
                (rs, rowNum) -> rs.getString("name"), 999L);
        assertThat(name).isNull();
    }

    @Test
    void queryForObjectRejectsMultipleRows() {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from items",
                (rs, rowNum) -> rs.getString("name")))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void sqlFailurePreservesCause() {
        assertThatThrownBy(() -> jdbcTemplate.update("insert into missing values (?)", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void mappingFailurePreservesCause() {
        assertThatThrownBy(() -> jdbcTemplate.query("select name from items",
                (rs, rowNum) -> rs.getString("missing")))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }
}
