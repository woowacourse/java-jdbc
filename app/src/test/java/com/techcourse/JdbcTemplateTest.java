package com.techcourse;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.ArgumentPreparedStatementSetter;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import nextstep.jdbc.RowMapperResultSetExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JdbcTemplateTest 기능 테스트")
public class JdbcTemplateTest {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("users_id"),
            rs.getString("users_account"),
            rs.getString("users_password"),
            rs.getString("users_email")
    );
    private static final JdbcTemplate JDBC_TEMPLATE = new JdbcTemplate(DataSourceConfig.getInstance());

    static {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("gumpAccount", "1a2w3e4r", "gump@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        JDBC_TEMPLATE.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @Test
    void queryForList() {
        //given
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users";
        //when
        final List<Map<String, Object>> result = JDBC_TEMPLATE.queryForList(sql);
        //then
        assertThat(result).isNotEmpty();
    }

    @Test
    void queryForObject() {
        //given
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users " +
                "where account = ?";
        //when
        final User foundUser = JDBC_TEMPLATE.queryForObject(sql, USER_ROW_MAPPER, user.getAccount());
        //then
        assertThat(foundUser).isNotNull();
    }

    @Test
    void query() {
        //given
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users " +
                "where account = ?";
        //when
        final List<User> user = JDBC_TEMPLATE.query(sql, USER_ROW_MAPPER, new Object[]{this.user.getAccount()});
        //then
        assertThat(user).isNotEmpty();
    }

    @Test
    void queryWithResultSetExtractor() {
        //given
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users " +
                "where account = ?";
        //when
        final List<User> user = JDBC_TEMPLATE.query(sql, new RowMapperResultSetExtractor<>(USER_ROW_MAPPER), new Object[]{this.user.getAccount()});
        //then
        assertThat(user).isNotEmpty();
    }

    @Test
    void queryWithPreparedStatementSetter() {
        //given
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users " +
                "where account = ?";
        //when
        final List<User> user = JDBC_TEMPLATE.query(conn -> conn.prepareStatement(sql), new ArgumentPreparedStatementSetter(new Object[]{this.user.getAccount()}), new RowMapperResultSetExtractor<>(USER_ROW_MAPPER));
        //then
        assertThat(user).isNotEmpty();
    }
}
