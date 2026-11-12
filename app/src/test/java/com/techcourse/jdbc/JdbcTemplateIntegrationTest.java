package com.techcourse.jdbc;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateIntegrationTest {
    @Test
    void bindsParametersMapsRowsAndReportsSqlErrors() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        var template = new JdbcTemplate(dataSource);
        template.update("create table entries (id bigint, name varchar(100))");
        assertThat(template.update("insert into entries values (?, ?)", 1L, "first")).isEqualTo(1);
        template.update("insert into entries values (?, ?)", 2L, "second");

        assertThat(template.query("select name from entries where id > ? order by id",
                rs -> rs.getString("name"), 0L)).containsExactly("first", "second");
        assertThat(template.<String>queryForObject("select name from entries where id = ?",
                rs -> rs.getString("name"), 99L)).isNull();
        assertThatThrownBy(() -> template.queryForObject("select name from entries",
                rs -> rs.getString("name"))).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> template.update("insert into missing_table values (?)", 1))
                .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(java.sql.SQLException.class);
    }
}
