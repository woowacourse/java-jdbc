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

    protected final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> ResultSet executeQuery(SqlSetter<T> sqlSetter, PreparedStatement preparedStatement) {
        try {
            sqlSetter.injectParams(preparedStatement);
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
        SqlSetter<T> sqlSetter = new SqlSetter<>(params);
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement psmt = conn.prepareStatement(sql);
             ResultSet resultSet = executeQuery(sqlSetter, psmt);) {

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
    }

    public <T> int nonSelectQuery(String sql, Object... params) {
        SqlSetter<T> sqlSetter = new SqlSetter<>(params);
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql);){
            sqlSetter.injectParams(pstmt);
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();

        } catch (SQLException | DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }
}
