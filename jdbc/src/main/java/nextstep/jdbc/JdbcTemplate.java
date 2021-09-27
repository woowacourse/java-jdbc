package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query, Object... values) {

        PreparedStatementSetter pstmtSetter = new valuesPreparedStatementSetter(values);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            log.debug("query : {}", query);

            pstmtSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper, Object... values) {

        PreparedStatementSetter pstmtSetter = new valuesPreparedStatementSetter(values);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(query, conn, pstmtSetter);
             ResultSet rs = executeQuery(pstmt)) {

            log.debug("query : {}", query);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }

            throw new SQLException();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> List<T> queryForList(String query, RowMapper<T> rowMapper, Object... values) {
        PreparedStatementSetter pstmtSetter = new valuesPreparedStatementSetter(values);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(query, conn, pstmtSetter);
             ResultSet rs = executeQuery(pstmt)) {

            log.debug("query : {}", query);

            List<T> result = new ArrayList<>();

            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatement generatePreparedStatement(String query, Connection conn, PreparedStatementSetter pstmtSetter)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmtSetter.setValues(pstmt);

        return pstmt;
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }
}
