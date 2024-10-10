package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> userMapper = (resultSet) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    @BeforeEach
    void tearDown() {
        truncateUser(dataSource);
    }

    JdbcTemplateTest() {
        this.dataSource = H2DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        DatabasePopulatorUtils.execute(this.dataSource);
    }

    @DisplayName("rowMapper로 객체 조회시 예외가 발생하지 않고 작동한다.")
    @Test
    void updateForObject() throws SQLException {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");

        // when
        assertDoesNotThrow(
                () -> jdbcTemplate.queryForObject("select * from users where id = 1", userMapper)
        );
    }

    @DisplayName("query와 rowMapper로 List조회가 가능하다")
    @Test
    void queryForListWithRowMapper() {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        executeUpdateQuery("insert into users (account, password, email) values('gugu2', '123', 'gugu2@naver.com')");

        // when
        List<User> users = jdbcTemplate.query("select * from users", userMapper);

        // then
        assertThat(users).hasSize(2);
    }

    @DisplayName("query(String, PreparedStatementSetter, RowMapper)로 List조회가 가능하다")
    @Test
    void queryForListWithRowMapperAndPreParedStatementSetter() {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu2@naver.com')");
        String sql = "select * from users where account = ?";

        // when
        List<User> users = jdbcTemplate.query(
                sql,
                (rs) -> {
                    rs.setString(1, "gugu");
                },
                userMapper
        );

        // then
        assertThat(users).hasSize(2);
    }

    @DisplayName("update로 insert가능하다")
    @Test
    void updateForInsert() throws SQLException {
        // given

        // when
        jdbcTemplate.update("insert into users (account, password, email) values ('gugu', '123', 'gugu@naver.com')");

        ResultSet resultSet = executeQuery("select count(*) from users");
        resultSet.next();
        int rowCount = resultSet.getInt(1);

        // then
        assertThat(rowCount).isOne();
    }

    @DisplayName("update로 UpdateQuery가 가능하다")
    @Test
    void updateForUpdateQuery() throws SQLException {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        String updateSql = String.format(" update users set account = '%s' where id = %d ", "updateGugu", 1);

        // when
        jdbcTemplate.update(updateSql);

        ResultSet resultSet = executeQuery("select account from users where id = 1");
        resultSet.next();
        String account = resultSet.getString(1);

        // then
        assertThat(account).isEqualTo("updateGugu");
    }

    @DisplayName("update(Stirng, PreparedStatementSetter)으로 insert 가능하다.")
    @Test
    void updateWithPreparedStatementSetterForInsert() throws SQLException {
        // given

        // when
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, "gugu");
                        ps.setString(2, "123");
                        ps.setString(3, "gugu@naver.com");
                    }
                });

        ResultSet resultSet = executeQuery("select count(*) from users");
        resultSet.next();
        int rowCount = resultSet.getInt(1);

        // then
        assertThat(rowCount).isOne();
    }

    @DisplayName("update(Stirng, Object...)으로 insert 가능하다.")
    @Test
    void updateWithObjectArgsForInsert() throws SQLException {
        // given
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        // when
        jdbcTemplate.update(insertSql, "gugu", "1234", "gugu@naver.com");

        ResultSet resultSet = executeQuery("select count(*) from users");
        resultSet.next();
        int rowCount = resultSet.getInt(1);

        // then
        assertThat(rowCount).isOne();
    }


    @DisplayName("update(Stirng, PreparedStatementSetter)으로 update 가능하다.")
    @Test
    void updateWithPreparedStatementSetterForUpdate() throws SQLException {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        String updateSql = " update users set account = ? where id = ? ";

        // when
        jdbcTemplate.update(updateSql,
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, "updateGugu");
                        ps.setLong(2, 1);
                    }
                });

        ResultSet resultSet = executeQuery("select account from users where id = 1");
        resultSet.next();
        String account = resultSet.getString(1);

        // then
        assertThat(account).isEqualTo("updateGugu");
    }

    @DisplayName("update(Stirng, object...)으로 update 가능하다.")
    @Test
    void updateWithObjectForUpdate() throws SQLException {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        String updateSql = " update users set account = ? where id = ? ";

        // when
        jdbcTemplate.update(updateSql, "updateGugu", 1);

        ResultSet resultSet = executeQuery("select account from users where id = 1");
        resultSet.next();
        String account = resultSet.getString(1);

        // then
        assertThat(account).isEqualTo("updateGugu");
    }

    @DisplayName("queryForObject(String, PreparedStatementSetter, RowMapper<>를 통해 select가능하다.")
    @Test
    void queryForObjectWithPreparedStatementSetter() {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        executeUpdateQuery("insert into users (account, password, email) values('gugu2', '123', 'gugu2@naver.com')");
        String selectQuery = "select * from users where account = ?";

        // when
        User user = jdbcTemplate.queryForObejct(
                selectQuery,
                (rs) -> {
                    rs.setString(1, "gugu");
                },
                userMapper);

        // then
        assertThat(user.email).isEqualTo("gugu@naver.com");
    }

    @DisplayName("queryForObject(String, PreparedStatementSetter, RowMapper<>)의 조회수가 1이아니면 예외가 발생한다")
    @Test
    void throwException_when_queryForObjectWithPreparedStatementSetterResultSizeIsNotOne() {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu2@naver.com')");
        String selectQuery = "select * from users where account = ?";

        // when - then
        assertThatThrownBy(
                () -> jdbcTemplate.queryForObejct(
                        selectQuery,
                        (rs) -> {
                            rs.setString(1, "gugu");
                        },
                        userMapper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과가 하나가 아닙니다. size: 2");
    }

    @DisplayName("query(String, RowMapper, Object..)로 조회가 가능하다")
    @Test
    void queryWithObjectArgsAndRowMapper() {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu2@naver.com')");
        String selectQuery = "select * from users where account = ?";
        // when
        List<User> users = jdbcTemplate.query(selectQuery, userMapper, "gugu");

        // then
        assertThat(users).hasSize(2);
    }

    @DisplayName("queryForObject(String, RowMapper, Object..)로 조회가 가능하다")
    @Test
    void queryForObjectWithObjectArgsAndRowMapper() {
        // given
        executeUpdateQuery("insert into users (account, password, email) values('gugu', '123', 'gugu@naver.com')");
        executeUpdateQuery("insert into users (account, password, email) values('gugu2', '123', 'gugu2@naver.com')");
        String selectQuery = "select * from users where account = ?";

        // when
        User user = jdbcTemplate.queryForObejct(selectQuery, userMapper, "gugu");

        // then
        assertThat(user.email).isEqualTo("gugu@naver.com");
    }

    private static void truncateUser(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM users");
            statement.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(String sql) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int executeUpdateQuery(String sql) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static class User {

        private final Long id;
        private final String account;
        private final String password;
        private final String email;

        public User(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }
    }


}
