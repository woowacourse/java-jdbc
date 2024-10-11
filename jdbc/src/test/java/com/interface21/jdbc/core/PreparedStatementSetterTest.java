package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.utils.DataSourceConfig;
import com.interface21.jdbc.utils.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreparedStatementSetterTest {

    private DataSource dataSource = DataSourceConfig.getInstance();
    private PreparedStatementSetter statementSetter = new PreparedStatementSetter();

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.createTables(dataSource);
    }

    @AfterEach
    void cleanUp() {
        DatabasePopulatorUtils.truncateTables(dataSource);
    }

    @DisplayName("sql문에 인자를 넣어줄 수 있다.")
    @Test
    void setValues() throws SQLException {
        // given
        String sql = "select id, account, password, email from users where account = ? and password = ? and account = ?";
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        // when
        statementSetter.setValues(preparedStatement, "ddang", "password", "ddang@email.com");

        // then
        assertThat(preparedStatement.toString()).contains("ddang", "password", "ddang@email.com");
    }
}
