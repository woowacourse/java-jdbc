package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.DataAccessUtils;
import nextstep.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query, Object... values) throws DataAccessException {
        execute(query, PreparedStatement::executeUpdate, values);
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper, Object... values) {
        List<T> results = query(query, rowMapper, values);

        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> List<T> query(String query, ResultSetExtractor<List<T>> extractor, Object... values) {
        return execute(query, pstmt -> {
            try (ResultSet rs = executeQuery(pstmt)) {

                log.debug("query : {}", query);
                return extractor.extract(rs);
            }
        }, values);
    }

    public <T> List<T> query(String query, RowMapper<T> rowMapper, Object... values) {
        return query(query, new RowMapperResultSetExtractorImpl(rowMapper), values);
    }

    private <T> T execute(String query, PreparedStateCallBack<T> action, Object... values) {
        PreparedStatementSetter pstmtSetter = new ValuesPreparedStatementSetter(values);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = generatePreparedStatement(query, conn, pstmtSetter)) {

            return action.doAction(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatement generatePreparedStatement(String query, Connection conn,
            PreparedStatementSetter pstmtSetter)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmtSetter.setValues(pstmt);

        return pstmt;
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }
}
