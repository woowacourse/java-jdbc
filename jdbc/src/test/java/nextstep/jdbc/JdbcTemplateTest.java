package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import nextstep.jdbc.exception.EmptyResultDataException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JdbcTemplate 단위 테스트")
class JdbcTemplateTest {

    private static final JdbcDataSource JDBC_DATA_SOURCE = new JdbcDataSource();
    private static final RowMapper<User> ROW_MAPPER = (resultSet, rowNum) -> {
        long id = resultSet.getLong("id");
        String account = resultSet.getString("account");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        return new User(id, account, password, email);
    };

    static {
        JDBC_DATA_SOURCE.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        JDBC_DATA_SOURCE.setUser("");
        JDBC_DATA_SOURCE.setPassword("");
    }

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBC_DATA_SOURCE);

    @BeforeEach
    void setUp() throws SQLException {
        String schema = "create table if not exists users (\n"
            + "    id bigint auto_increment,\n"
            + "    account varchar(100) not null,\n"
            + "    password varchar(100) not null,\n"
            + "    email varchar(100) not null,\n"
            + "    primary key(id)\n"
            + ");\n";
        try (Connection connection = JDBC_DATA_SOURCE.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(schema)) {
            preparedStatement.execute();
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        String sql = "drop table users";
        try (Connection connection = JDBC_DATA_SOURCE.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.execute();
        }
    }

    @DisplayName("update 메서드로 데이터를 저장하며, queryForObject로 조회한다.")
    @Test
    void update_and_queryForObject() {
        // given
        String insertQuery = "insert into users(account, password, email) values(?, ?, ?);";
        String selectQuery = "select * from users where account = ?";

        // when
        int rowCounts = jdbcTemplate.update(insertQuery, "gugu", "123", "woowa@naver.com");
        User user = jdbcTemplate.queryForObject(selectQuery, ROW_MAPPER, "gugu");

        // then
        assertThat(rowCounts).isOne();
        assertThat(user.getId()).isNotNull();
        assertThat(user).usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new User("gugu", "123", "woowa@naver.com"));
    }

    @DisplayName("update 메서드로 수정하면 영향 받은 ROW 개수가 반환되며, queryForList로 조회한다.")
    @Test
    void update_and_queryForList() {
        // given
        String insertQuery = "insert into users(account, password, email) values(?, ?, ?);";
        jdbcTemplate.update(insertQuery, "gugu", "123", "woowa@naver.com");
        jdbcTemplate.update(insertQuery, "gugu2", "1234", "woowa2@naver.com");
        jdbcTemplate.update(insertQuery, "gugu3", "1234", "woowa3@naver.com");

        // when
        String sql = "update users set account = ? where password = ?";
        String selectQuery = "select * from users";
        int rowCounts = jdbcTemplate.update(sql, "kevin", "1234");
        List<User> users = jdbcTemplate.queryForList(selectQuery, ROW_MAPPER);

        // then
        assertThat(rowCounts).isEqualTo(2);
        assertThat(users).extracting("account")
            .contains("gugu", "kevin", "kevin");
    }

    @DisplayName("queryForList 메서드는 해당 데이터가 없는 경우 빈 리스트를 반환한다.")
    @Test
    void queryForList_empty() {
        // given
        String query = "select * from users";

        // when
        List<User> users = jdbcTemplate.queryForList(query, ROW_MAPPER);

        // then
        assertThat(users).isEmpty();
    }

    @DisplayName("queryForObject 메서드는")
    @Nested
    class Describe_queryForObject {

        @DisplayName("결과값이 0개일 때")
        @Nested
        class Context_result_size_empty {

            @DisplayName("EmptyResultDataException이 발생한다.")
            @Test
            void it_throws_EmptyResultDataException() {
                // given
                String sql = "select * from users where id = ?";

                // when, then
                assertThatCode(() -> jdbcTemplate.queryForObject(sql, ROW_MAPPER, "13121"))
                    .isInstanceOf(EmptyResultDataException.class)
                    .hasMessage("Expected single data, but it was empty result!");
            }
        }

        @DisplayName("결과값이 1개일 때")
        @Nested
        class Context_result_size_single {

            @DisplayName("정상적으로 데이터가 반환된다.")
            @Test
            void it_returns_target() {
                // given
                String insertQuery = "insert into users(account, password, email) values(?, ?, ?);";
                jdbcTemplate.update(insertQuery, "gugu", "123", "woowa@naver.com");
                String sql = "select * from users where account = ?";

                // when
                User user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, "gugu");

                // then
                assertThat(user.getId()).isNotNull();
                assertThat(user).usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(new User("gugu", "123", "woowa@naver.com"));
            }
        }

        @DisplayName("결과값이 2개 이상일 때")
        @Nested
        class Context_result_size_more_than_two {

            @DisplayName("IncorrectResultSizeDataAccessException 예외가 발생한다.")
            @Test
            void it_throws_IncorrectResultSizeDataAccessException() {
                // given
                String insertQuery = "insert into users(account, password, email) values(?, ?, ?);";
                jdbcTemplate.update(insertQuery, "gugu", "123", "woowa@naver.com");
                jdbcTemplate.update(insertQuery, "gugu2", "123", "woowa2@naver.com");
                String sql = "select * from users where password = ?";

                // when, then
                assertThatCode(() -> jdbcTemplate.queryForObject(sql, ROW_MAPPER, "123"))
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                    .hasMessage("Expected single data, but it was more than one result!");
            }
        }
    }

    private static class User {

        private Long id;
        private final String account;
        private String password;
        private final String email;

        public User(long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public User(String account, String password, String email) {
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }
    }
}
