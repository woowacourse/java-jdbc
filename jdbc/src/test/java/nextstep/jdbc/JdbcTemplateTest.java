package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nextstep.jdbc.exception.JdbcNotFoundException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final TestUserRowMapper TEST_USER_ROW_MAPPER = new TestUserRowMapper();
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        JdbcDataSource dataSource = getJdbcDataSource();

        final String sql = readSqlFile();

        try (Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement()) {
            statement.execute(sql);
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private JdbcDataSource getJdbcDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        dataSource.setUser("");
        dataSource.setPassword("");
        return dataSource;
    }

    private String readSqlFile() throws IOException {
        final URL url = getClass().getClassLoader().getResource("schema.sql");
        final File file = new File(url.getFile());
        final String sql = Files.readString(file.toPath());
        return sql;
    }

    @DisplayName("sql문을 통해 단일 데이터 조회")
    @Test
    void query() {
        TestUser user = jdbcTemplate.query("select id, account, password, email from users where account = ?", TEST_USER_ROW_MAPPER, "junroot");

        assertThat(user.getAccount()).isEqualTo("junroot");
        assertThat(user.getPassword()).isEqualTo("rootzzang123");
        assertThat(user.getEmail()).isEqualTo("rootjjang@gmail.com");
    }

    @DisplayName("만족하는 데이터가 없는 경우 예외 처리")
    @Test
    void InvalidQuery() {
        assertThatThrownBy(() -> jdbcTemplate.query("select id, account, password, email from users where account = ?", new TestUserRowMapper(), "junriot"))
            .isExactlyInstanceOf(JdbcNotFoundException.class);
    }

    @DisplayName("sql문을 통해 데이터 전체 조회")
    @Test
    void queryAsList() {
        List<TestUser> users = jdbcTemplate.queryAsList("select id, account, password, email from users", TEST_USER_ROW_MAPPER);

        assertThat(users).hasSize(2);
    }

    @DisplayName("update를 통해 영향을 받은 행의 개수 반환")
    @Test
    void update() {
        int result1 = jdbcTemplate.update("insert into users (account, password, email) values ('junroot3', 'rootzzang1234', 'rootjjang@gmail.com')");
        assertThat(result1).isEqualTo(1);

        int result2 = jdbcTemplate.update("update users set account = 'root123' where email = 'rootjjang@gmail.com'");
        assertThat(result2).isEqualTo(2);
    }

    @DisplayName("execute를 통해 DDL 실행")
    @Test
    void execute() {
        jdbcTemplate.execute("create table test_table (id bigint auto_increment, name varchar(100) not null)");

        int updatedRow = jdbcTemplate.update("insert into test_table (name) values ('junroot')");
        assertThat(updatedRow).isEqualTo(1);

        Map<String, Object> row = jdbcTemplate.query("select * from test_table",
            resultSet -> {
                HashMap<String, Object> results = new HashMap<>();
                results.put("id", resultSet.getLong(1));
                results.put("name", resultSet.getString(2));
                return results;
            });

        assertThat(row.get("id")).isNotNull();
        assertThat(row.get("name")).isEqualTo("junroot");
    }
}
