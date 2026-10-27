package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:jdbc_template_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update("CREATE TABLE sample (id BIGINT PRIMARY KEY, note VARCHAR(20))");
    }

    @Test
    void updateWithPreparedStatementSetterBindsParameters() {
        jdbcTemplate.update("INSERT INTO sample (id, note) VALUES (?, ?)", 1L, "before");

        jdbcTemplate.update("UPDATE sample SET note = ? WHERE id = ?", pstmt -> {
            pstmt.setString(1, "after");
            pstmt.setLong(2, 1L);
        });

        final var notes = jdbcTemplate.query(
                "SELECT note FROM sample WHERE id = ?",
                (rs, rowNum) -> rs.getString("note"),
                1L
        );
        assertThat(notes).containsExactly("after");
    }

    @Test
    void queryWithPreparedStatementSetterBindsParameters() {
        jdbcTemplate.update("INSERT INTO sample (id, note) VALUES (?, ?)", 1L, "first");
        jdbcTemplate.update("INSERT INTO sample (id, note) VALUES (?, ?)", 2L, "second");

        final var notes = jdbcTemplate.query(
                "SELECT note FROM sample WHERE id = ?",
                pstmt -> pstmt.setLong(1, 2L),
                (rs, rowNum) -> rs.getString("note")
        );
        assertThat(notes).containsExactly("second");
    }

    @Test
    void updateWithSingleNullArgument() throws SQLException {
        jdbcTemplate.update("INSERT INTO sample (id, note) VALUES (1, 'before')");

        jdbcTemplate.update("UPDATE sample SET note = ? WHERE id = 1", new Object[]{null});

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT note FROM sample WHERE id = 1")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("note")).isNull();
        }
    }
}
