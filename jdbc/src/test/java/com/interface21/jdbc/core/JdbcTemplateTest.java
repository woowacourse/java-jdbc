package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.jdbc.ObjectMapper;
import com.interface21.jdbc.TestUser;
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
    private final ObjectMapper<TestUser> objectMapper = (rs) -> new TestUser(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
    );
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
        TestUser result = jdbcTemplate.query(objectMapper, sql, param);

        // then
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(1L),
                () -> assertThat(result.getAccount()).isEqualTo("gugu"),
                () -> assertThat(result.getPassword()).isEqualTo("123"),
                () -> assertThat(result.getEmail()).isEqualTo("gugu@naver.com")
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
        List<TestUser> results = jdbcTemplate.queryList(objectMapper, sql);

        // then
        assertAll(
                () -> assertThat(results.get(0).getId()).isEqualTo(1L),
                () -> assertThat(results.get(0).getAccount()).isEqualTo("gugu"),
                () -> assertThat(results.get(0).getPassword()).isEqualTo("123"),
                () -> assertThat(results.get(0).getEmail()).isEqualTo("gugu@naver.com"),
                () -> assertThat(results.get(1).getId()).isEqualTo(2L),
                () -> assertThat(results.get(1).getAccount()).isEqualTo("gugu2"),
                () -> assertThat(results.get(1).getPassword()).isEqualTo("1232"),
                () -> assertThat(results.get(1).getEmail()).isEqualTo("gugu2@naver.com")
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
