package org.springframework.jdbc.core;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.IncorrectResultSizeDataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int update(String sql, Object... args) throws DataAccessException {
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            for (int i = 1; i <= args.length; i++) {
                pstmt.setObject(i, args[i - 1]);
            }
            return pstmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args)
            throws DataAccessException {
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            for (int i = 1; i <= args.length; i++) {
                pstmt.setObject(1, args[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = rs.getMetaData().getColumnCount();

            Class<?>[] columnTypes = new Class[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                int columnType = metaData.getColumnType(i);
                columnTypes[i - 1] = ColumnTypes.convertToClass(columnType);
            }
            Constructor<?> constructor = requiredType.getDeclaredConstructor(columnTypes);
            if (rs.first() && rs.isLast()) {
                Object[] initargs = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    initargs[i - 1] = rs.getObject(i);
                }
                return requiredType.cast(constructor.newInstance(initargs));
            }
            throw new IncorrectResultSizeDataAccessException();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, 0));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
