package transaction.stage1;

import transaction.stage1.jdbc.JdbcTemplate;
import transaction.stage1.jdbc.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void updatePasswordGreaterThan(final String password, final long id) {
        final var sql = "update users set password = ? where id >= ?";
        jdbcTemplate.update(sql, password, id);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, createRowMapper(), id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, createRowMapper(), account);
    }

    public List<User> findGreaterThan(final long id) {
        final var sql = "select id, account, password, email from users where id >= ?";
        return jdbcTemplate.query(sql, createRowMapper(), id);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, createRowMapper());
    }

    private static RowMapper<User> createRowMapper() {
        return (final var rs) -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }
}
