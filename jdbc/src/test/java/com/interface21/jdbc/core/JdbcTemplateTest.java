package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("SQLException이 발생하면 DataAccessException으로 변환한다.")
    @Test
    void exceptionTest1() throws SQLException {
        // given

        String sql = "SELECT id, account, password, email FROM users";
        given(dataSource.getConnection()).willThrow(new SQLException());

        // when
        // then
        assertThatThrownBy(() -> jdbcTemplate.query(sql, (rs, rowNum) -> null))
                .isInstanceOf(DataAccessException.class);
    }

}
