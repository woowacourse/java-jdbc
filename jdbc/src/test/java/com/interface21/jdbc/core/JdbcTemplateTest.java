package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.jdbc.ObjectMapper;
import com.interface21.jdbc.PreparedStatementSetter;
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
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
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
    void queryForObject() {
        // given
        String sql = "select id, account, password, email from users where id = ?";
        long id = 1L;

        // when
        TestUser result = jdbcTemplate.queryForObject(objectMapper, sql, pstmt -> pstmt.setObject(1, id));

        // then
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(id),
                () -> assertThat(result.getAccount()).isEqualTo("gugu"),
                () -> assertThat(result.getPassword()).isEqualTo("123"),
                () -> assertThat(result.getEmail()).isEqualTo("gugu@naver.com")
        );
    }

    @Test
    void queryForObjectList() throws SQLException {
        // given
        preparedStatement = connection.prepareStatement(
                "insert into users (account, password, email) values ('gugu2', '1232', 'gugu2@naver.com')");
        preparedStatement.execute();
        String sql = "select id, account, password, email from users";

        // when
        List<TestUser> results = jdbcTemplate.query(objectMapper, sql, pstmt -> {});

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
        PreparedStatementSetter preparedStatementSetter = pstmt -> {
            pstmt.setObject(1, "kirby");
            pstmt.setObject(2, "111");
            pstmt.setObject(3, "kirby@naver.com");
            pstmt.setObject(4, 1L);
        };
        jdbcTemplate.execute(sql, preparedStatementSetter);

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
