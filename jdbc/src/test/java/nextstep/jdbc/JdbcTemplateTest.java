package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class JdbcTemplateTest {

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());


    @BeforeEach
    public void setUp() {
        final DataSource instance = DataSourceConfig.getInstance();
        try (final Connection connection = instance.getConnection()) {
            final Statement statement = connection.createStatement();
            statement.execute("drop table if exists users");
            statement.execute("create table if not exists users (id bigint auto_increment, account varchar(255), password varchar(255), email varchar(255), primary key (id))");
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }

    }

    @Test
    void update() {
        //given
        //when
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, "account");
            preparedStatement.setString(2, "password");
            preparedStatement.setString(3, "email");
        });

        //then
        RowMapper<Map<String, Object>> rowMapper = (resultSet, rowNum) -> Map.of(
                "id", resultSet.getLong("id"),
                "account", resultSet.getString("account"),
                "password", resultSet.getString("password"),
                "email", resultSet.getString("email")
        );
        String selectSql = "select * from users";
        final List<Map<String, Object>> result = jdbcTemplate.query(selectSql, rowMapper);
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0).get("account")).isEqualTo("account");
            softly.assertThat(result.get(0).get("password")).isEqualTo("password");
            softly.assertThat(result.get(0).get("email")).isEqualTo("email");
        });
    }

    @Test
    public void queryList() {
        //given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, "account");
            preparedStatement.setString(2, "password");
            preparedStatement.setString(3, "email");
        });
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, "account2");
            preparedStatement.setString(2, "password2");
            preparedStatement.setString(3, "email2");
        });

        //when
        String selectSql = "select * from users";
        final List<Map<String, Object>> result = jdbcTemplate.query(selectSql, (resultSet, rowNum) -> Map.of(
                "id", resultSet.getLong("id"),
                "account", resultSet.getString("account"),
                "password", resultSet.getString("password"),
                "email", resultSet.getString("email")
        ));

        //then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
        });
    }

    @Test
    @DisplayName("가변인자를 이용한 쿼리 테스트")
    void vargsQuery() {
        //given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, "account");
            preparedStatement.setString(2, "password");
            preparedStatement.setString(3, "email");
        });

        //when
        String selectSql = "select * from users where account = ?";
        final Map<String, Object> result = jdbcTemplate.query(selectSql, (resultSet, rowNum) -> Map.of(
                "id", resultSet.getLong("id"),
                "account", resultSet.getString("account"),
                "password", resultSet.getString("password"),
                "email", resultSet.getString("email")
        ), "account");

        //then
        assertSoftly(softly -> {
            softly.assertThat(result.get("account")).isEqualTo("account");
            softly.assertThat(result.get("password")).isEqualTo("password");
            softly.assertThat(result.get("email")).isEqualTo("email");
        });

    }
}
