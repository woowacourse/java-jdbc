package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, createPreparedStatementSetter(args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rm, Object... args) {
        return query(sql, new RowMapperResultSetExtractor<>(rm),
                createPreparedStatementSetter(args));
    }

    public void execute(String sql, PreparedStatementSetter pss) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T query(String sql, ResultSetExtractor<T> extractor, PreparedStatementSetter pss) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = executeQuery(pstmt, pss)
        ) {
            log.debug("query : {}", sql);

            return extractor.extractData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt, PreparedStatementSetter pss)
            throws SQLException {
        pss.setValues(pstmt);
        return pstmt.executeQuery();
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object[] args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}
