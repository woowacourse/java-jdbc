package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> ResultSet executeQuery(PreparedStatement preparedStatement, Object... params) {
        try {
            SqlSetter.injectParams(preparedStatement, params);
            return preparedStatement.executeQuery();
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public <T> List<T> selectQuery(String sql, JdbcMapper<T> jdbcMapper, Object... params) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement psmt = conn.prepareStatement(sql);
             ResultSet resultSet = executeQuery(psmt, params);) {

            log.debug("query : {}", sql);
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(jdbcMapper.mapRow(resultSet));
            }
            return results;

        } catch (SQLException | DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
        finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public <T> int nonSelectQuery(String sql, Object... params) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql);){
            SqlSetter.injectParams(pstmt, params);
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();

        } catch (SQLException | DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
        finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
