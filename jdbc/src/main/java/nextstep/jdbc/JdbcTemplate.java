package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, @Nullable Object... args) {
        return update(connection -> connection.prepareStatement(sql),
                new ArgumentTypePreparedStatementSetter(args));
    }

    public int update(PreparedStatementCreator preparedStatementCreator) {
        return update(preparedStatementCreator, null);
    }

    public int update(PreparedStatementCreator preparedStatementCreator,
                      PreparedStatementSetter preparedStatementSetter) {
        return execute(preparedStatementCreator, pstmt -> {
            if (Objects.nonNull(preparedStatementSetter)) {
                preparedStatementSetter.setValue(pstmt);
            }
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        List<T> results = query(connection -> connection.prepareStatement(sql),
                new ArgumentTypePreparedStatementSetter(args),
                new ResultSetExtractor<>(rowMapper));
        return results.stream()
                .findFirst()
                .orElseThrow(DataAccessException::new);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query((connection -> connection.prepareStatement(sql)),
                null,
                new ResultSetExtractor<>(rowMapper));
    }

    public <T> List<T> query(PreparedStatementCreator preparedStatementCreator,
                             PreparedStatementSetter preparedStatementSetter,
                             ResultSetExtractor<T> resultSetExtractor) {
        return execute(preparedStatementCreator, (pstmt -> {
            ResultSet rs = null;
            try {
                if (Objects.nonNull(preparedStatementSetter)) {
                    preparedStatementSetter.setValue(pstmt);
                }
                rs = pstmt.executeQuery();
                return resultSetExtractor.extract(rs);
            } finally {
                closeResultSet(rs);
            }
        }));
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private <T> T execute(PreparedStatementCreator preparedStatementCreator,
                          PreparedStatementCallback<T> preparedStatementCallback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = preparedStatementCreator.createPreparedStatement(conn)) {
            return preparedStatementCallback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

}
