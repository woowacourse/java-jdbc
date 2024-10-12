package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static JdbcTemplate jdbcTemplate;
    private static Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeAll
    static void setConnection() throws SQLException {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        connection = DataSourceConfig.getInstance().getConnection();
    }

    @BeforeEach
    void setup() throws SQLException {
        String truncate = "drop table if exists users;";
        String createTable = """
                    create table users (
                    id bigint auto_increment,
                    account varchar(100) not null,
                    password varchar(100) not null,
                    email varchar(100) not null,
                    primary key(id)
                );
                """;
        String initData = "insert into users (account, password, email) values ('gugu', '123', 'gugu@naver.com')";
        preparedStatement = connection.prepareStatement(truncate + createTable + initData);
        preparedStatement.execute();
    }

    @Test
    void query() {
        // given
        String sql = "select id, account, password, email from users where id = ?";
        long param = 1;

        // when
        List<Object> result = jdbcTemplate.query(sql, param);

        // then
        assertAll(
                () -> assertThat(result.get(0)).isEqualTo(1L),
                () -> assertThat(result.get(1)).isEqualTo("gugu"),
                () -> assertThat(result.get(2)).isEqualTo("123"),
                () -> assertThat(result.get(3)).isEqualTo("gugu@naver.com")
        );
    }

    @Test
    void queryList() throws SQLException {
        // given
        preparedStatement = connection.prepareStatement(
                "insert into users (account, password, email) values ('gugu2', '1232', 'gugu2@naver.com')");
        preparedStatement.execute();
        String sql = "select id, account, password, email from users";

        // when
        List<List<Object>> results = jdbcTemplate.queryList(sql);

        // then
        assertAll(
                () -> assertThat(results.get(0).get(0)).isEqualTo(1L),
                () -> assertThat(results.get(0).get(1)).isEqualTo("gugu"),
                () -> assertThat(results.get(0).get(2)).isEqualTo("123"),
                () -> assertThat(results.get(0).get(3)).isEqualTo("gugu@naver.com"),
                () -> assertThat(results.get(1).get(0)).isEqualTo(2L),
                () -> assertThat(results.get(1).get(1)).isEqualTo("gugu2"),
                () -> assertThat(results.get(1).get(2)).isEqualTo("1232"),
                () -> assertThat(results.get(1).get(3)).isEqualTo("gugu2@naver.com")
        );
    }

    @Test
    void execute() throws SQLException {
        // given
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        // when
        jdbcTemplate.execute(sql, "kirby", "111", "kirby@naver.com", 1L);

        // then
        preparedStatement = connection.prepareStatement("select account, password, email from users where id = 1");
        resultSet = preparedStatement.executeQuery();
        resultSet.next();
        assertAll(
                () -> assertThat(resultSet.getObject(1)).isEqualTo("kirby"),
                () -> assertThat(resultSet.getObject(2)).isEqualTo("111"),
                () -> assertThat(resultSet.getObject(3)).isEqualTo("kirby@naver.com")
        );
    }
}
