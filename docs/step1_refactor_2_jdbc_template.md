
> [돌아가기](../README.md)

```java
public class UserDao {
    
    // ...
    
    private static final ThrowingFunction<ResultSet, User, SQLException> USER_ROW_MAPPER =
            (ResultSet rs) -> new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));

    private final JdbcTemplate jdbcTemplate;

    // ...
    
    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.queryAll(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryOne(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryOne(sql, USER_ROW_MAPPER, account);
    }
}

```

```java
public class JdbcTemplate {

    // ...

    public void execute(final String sql, final Object... params) {
        log.debug("query : {}", sql);
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            setPstmtParams(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryOne(final String sql,
                          final ThrowingFunction<ResultSet, T, SQLException> rowMapper,
                          final Object... conditionParams) {

        log.debug("query : {}", sql);
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            setPstmtParams(pstmt, conditionParams);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.apply(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    public <T> List<T> queryAll(final String sql,
                                final ThrowingFunction<ResultSet, T, SQLException> userRowMapper) {
        log.debug("query : {}", sql);
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            rs = pstmt.executeQuery();
            List<T> resultRows = new ArrayList<>();
            while (rs.next()) {
                final T resultRow = userRowMapper.apply(rs);
                resultRows.add(resultRow);
            }
            return resultRows;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    private void setPstmtParams(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < List.of(params).size(); i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    private void closeResultSet(final ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
```