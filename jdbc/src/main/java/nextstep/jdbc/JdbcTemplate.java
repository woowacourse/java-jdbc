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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(PreparedStatementCreator preparedStatementCreator) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = preparedStatementCreator.createPreparedStatement(conn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object ... args) {
        List<T> result = query((connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }
            return pstmt;
        }), new ResultSetExtractor<T>(rowMapper));
        return result.get(0);
    }


    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query((connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            return pstmt;
        }), new ResultSetExtractor<T>(rowMapper));
    }

    public <T> List<T> query(PreparedStatementCreator preparedStatementCreator, ResultSetExtractor<T> resultSetExtractor) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = preparedStatementCreator.createPreparedStatement(conn);
            rs = pstmt.executeQuery();
            return resultSetExtractor.extract(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }

    }
}
