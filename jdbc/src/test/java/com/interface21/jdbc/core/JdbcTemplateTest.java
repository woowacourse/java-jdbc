package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @Test
    @DisplayName("DataSource와 연결에 실패하면 생성 시점에 예외가 발생한다.")
    void constructor() throws Exception {
        var dataSource = mock(DataSource.class);
        doThrow(SQLException.class).when(dataSource).getConnection();

        assertThatThrownBy(() -> new JdbcTemplate(dataSource));
    }
}
