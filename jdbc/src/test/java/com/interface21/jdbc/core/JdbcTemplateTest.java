package com.interface21.jdbc.core;

import com.interface21.jdbc.config.DataSourceConfig;
import com.interface21.jdbc.config.DatabasePopulatorUtils;
import com.interface21.jdbc.result.SingleSelectResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("Jdbc 템플릿을 통해 쿼리문을 실행 가능하다.")
    void execute_sql() {
        final var writeSql = "insert into test (content) values (?)";
        jdbcTemplate.command(
                writeSql, "sample"
        );

        final String selectSql = "select id, content from test where id = ?";
        final SingleSelectResult result = jdbcTemplate.querySingle(selectSql, 1);
        final String content = result.getColumnValue("content");
        assertThat(content).isEqualTo("sample");
    }
}
