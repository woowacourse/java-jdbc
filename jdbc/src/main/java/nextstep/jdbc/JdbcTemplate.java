package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setObjects(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        ResultSet resultSet = null;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setObjects(pstmt, args);
            resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(resultSet);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void setObjects(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        ResultSet resultSet = null;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            List<T> results = new ArrayList<>();
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(resultSet);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void closeResultSet(final ResultSet resultSet) {
        try {
            Objects.requireNonNull(resultSet);
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
