package org.springframework.jdbc.core;

import config.DataSourceConfig;
import init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class PreparedStatementCreatorTest {

    private DataSource dataSource;
    private PreparedStatementCreator preparedStatementCreator;

    @BeforeEach
    void setUp() {
        this.preparedStatementCreator = new PreparedStatementCreator();
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
    }

    @Test
    void createPreparedStatement() throws SQLException {
        // given
        final Connection connection = dataSource.getConnection();
        final String sql = "insert into herb(name, age) values(?, ?)";
        final Object[] args = {"mint", 25};

        // when
        final PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection, sql, args);
        final ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();

        // then
        assertAll(
                () -> assertThat(preparedStatement.getConnection()).isEqualTo(connection),
                () -> assertThat(parameterMetaData.getParameterCount()).isEqualTo(args.length),
                () -> assertThat(parameterMetaData.getParameterClassName(1)).contains("String"),
                () -> assertThat(parameterMetaData.getParameterClassName(2)).contains("Integer")
        );
    }
}
