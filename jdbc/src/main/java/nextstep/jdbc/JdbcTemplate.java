package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final int SINGLE_RESULT = 1;
    private static final int FIRST_ELEMENT = 0;
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int command(final String sql, final Object... params) {
        return execute(sql, null, ((rowMapper, pstmt) -> pstmt.executeUpdate()), params);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, rowMapper, (this::mapRows), params);
    }

    public <T> T queryForOne(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final var results = queryForList(sql, rowMapper, params);

        if (results.size() != SINGLE_RESULT) {
            throw new DataAccessException(String.format("Expected single result, but %s", results.size()));
        }

        return results.get(FIRST_ELEMENT);
    }

    private <T, R> R execute(final String sql, final RowMapper<T> rowMapper,
                             final ThrowingBiFunction<T, R> template, final Object... params) {
        try (
                final var connection = getConnection();
                final var pstmt = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            log.debug("params : {}", params);
            setParams(pstmt, List.of(params));

            return template.apply(rowMapper, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(final PreparedStatement pstmt, final List<Object> params) throws SQLException {
        if (Objects.isNull(params)) {
            return;
        }

        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }
    }

    private <T> List<T> mapRows(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        final var resultSet = pstmt.executeQuery();

        final var results = new ArrayList<T>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }

        return results;
    }
}
