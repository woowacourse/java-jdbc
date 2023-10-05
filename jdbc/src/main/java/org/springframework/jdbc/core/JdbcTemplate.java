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
    private final StatementGenerator statementGenerator;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.statementGenerator = new StatementGenerator();
    }

    public void update(String sql, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = statementGenerator.prepareStatement(sql, conn, params);
        ) {
            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = statementGenerator.prepareStatement(sql, conn, params);
                ResultSet rs = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);

            T result = null;
            if (rs.next()) {
                result = rowMapper.map(rs);
            }
            if (rs.next()) {
                throw new IllegalStateException("2개 이상의 결과가 존재합니다!");
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... params) {
        return queryForObject(
                sql,
                mapTo(requiredType),
                params
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = statementGenerator.prepareStatement(sql, conn, params);
                ResultSet rs = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.map(rs));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, Class<T> requiredType, Object... params) {
        return query(
                sql,
                mapTo(requiredType),
                params
        );
    }

    private <T> RowMapper<T> mapTo(Class<T> requiredType) {
        return rs -> {
            T instance = createInstance(requiredType);
            fillFields(requiredType, rs, instance);
            return instance;
        };
    }

    private <T> T createInstance(Class<T> requiredType) {
        try {
            Constructor<T> constructor = requiredType.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            constructor.setAccessible(false);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void fillFields(Class<T> requiredType, ResultSet rs, T instance) {
        Field[] fields = requiredType.getDeclaredFields();
        for (final Field field : fields) {
            final String fieldName = field.getName();
            try {
                for (int i = 0; i < fields.length; i++) {
                    final String columnName = rs.getMetaData().getColumnName(i + 1);
                    if (columnName.equalsIgnoreCase(fieldName)) {
                        field.setAccessible(true);
                        field.set(instance, rs.getObject(i + 1));
                        field.setAccessible(false);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
