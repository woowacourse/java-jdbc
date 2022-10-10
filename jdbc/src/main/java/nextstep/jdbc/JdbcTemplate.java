package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(Connection connection, final String sql, final Object... args) {
        execute(connection, sql, pstmt -> {
            setParameters(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(Connection connection, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(connection, sql, pstmt -> setParamsAndGetResult(rowMapper, pstmt, args));
    }

    public <T> T queryForObject(Connection connection, final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(connection, sql, rowMapper, args);
        return getSingleResult(results);
    }

    private <T> T execute(Connection connection, final String sql, final PreparedStatementCallback<T> preparedStatementCallback) {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            return preparedStatementCallback.doPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private <T> List<T> setParamsAndGetResult(RowMapper<T> rowMapper, PreparedStatement pstmt, Object[] args)
            throws SQLException {
        final List<T> results = new ArrayList<>();
        setParameters(pstmt, args);
        final ResultSet resultSet = pstmt.executeQuery();

        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... args)
            throws SQLException {
        PreparedStatementSetter preparedStatementSetter = (ps, objects) ->
        {
            for (int i = 0; i < objects.length; i++) {
                ps.setObject(i + 1, objects[i]);
            }
        };
        preparedStatementSetter.setValues(preparedStatement, args);
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("잘못된 결과입니다.");
        }
        return results.iterator().next();
    }
}
