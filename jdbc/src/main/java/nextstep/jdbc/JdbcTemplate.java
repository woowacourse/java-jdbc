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

    private void update(String sql, PreparedStatementSetter pss) {
        execute(sql, preparedStatement -> {
            pss.setValue(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    public void update(String sql, Object... args) {
        update(sql, new ArgumentPreparedStatementSetter(args));
    }

    private <T> T queryForObject(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) {

        return execute(sql, pstmt -> {
            pss.setValue(pstmt);
            ResultSet rs = pstmt.executeQuery();
            return rse.extractData(rs);
        });
    }

    private <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) throws
        DataAccessException {
        return queryForObject(sql, pss, new RowMapperResultSetExtractor<>(rowMapper));

    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return queryForObject(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    private <T> List<T> query(String sql, ResultSetExtractor<T> rse){
        return execute(sql, pstmt -> {
            ResultSet rs = pstmt.executeQuery();
            return rse.extractList(rs);
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

    private <T> T execute(String sql, StatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            return callback.doInStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }
}
