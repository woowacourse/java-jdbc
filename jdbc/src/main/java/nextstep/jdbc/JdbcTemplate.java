package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.QueryException;
import nextstep.jdbc.exception.UpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        PreparedStatementSetter pstmtSetter = new ArgumentPreparedStatementSetter(args);

        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            LOG.debug("query: {}", sql);

            pstmtSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new UpdateException();
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = executeQuery(pstmt)
        ) {
            LOG.debug("query: {}", sql);

            List<T> objects = new ArrayList<>();
            while (rs.next()) {
                objects.add(rowMapper.mapRow(rs));
            }
            return objects;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new QueryException();
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        PreparedStatementSetter pstmtSetter = new ArgumentPreparedStatementSetter(args);
        ResultSet rs = null;

        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            LOG.debug("query: {}", sql);

            pstmtSetter.setValues(pstmt);
            rs = executeQuery(pstmt);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new QueryException();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) {
        try {
            return pstmt.executeQuery();
        } catch (Exception e) {
            throw new QueryException();
        }
    }
}
