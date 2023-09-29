package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public void update(String sql, Object... params) {
        executeQueryWithoutResult(sql, params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return executeQueryWithResult(sql, params, rs -> {
            T result = null;
            if (rs.next()) {
                result = rowMapper.map(rs);
            }
            if (rs.next()) {
                throw new IllegalStateException("2개 이상의 결과가 존재합니다!");
            }
            return result;
        });
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... params) {
        return queryForObject(sql,
                rs -> {
                    try {
                        Constructor<T> constructor = requiredType.getDeclaredConstructor();
                        constructor.setAccessible(true);

                        T instance = constructor.newInstance();
                        Field[] fields = requiredType.getDeclaredFields();
                        constructor.setAccessible(false);

                        for (int i = 0; i < fields.length; i++) {
                            Field field = fields[i];
                            field.setAccessible(true);
                            field.set(instance, rs.getObject(1 + i));
                            field.setAccessible(false);
                        }
                        return instance;
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                },
                params
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return executeQueryWithResult(sql, params, rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.map(rs));
            }
            return result;
        });
    }

    private void executeQueryWithoutResult(String sql, Object[] params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            fillParameters(params, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T executeQueryWithResult(String sql, Object[] params, ResultFormatter<T> resultFormatter) {
        ResultSet rs = null;
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            fillParameters(params, pstmt);
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            return resultFormatter.format(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void fillParameters(Object[] params, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(1 + i, params[i]);
        }
    }
}
