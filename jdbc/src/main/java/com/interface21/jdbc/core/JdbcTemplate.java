package com.interface21.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(String sql, Consumer<PreparedStatement> beforeExecution) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            beforeExecution.accept(pstmt);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public <T> T executeQuery(String sql, Consumer<PreparedStatement> beforeExecution, Class<T> dataType) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            beforeExecution.accept(pstmt);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                return null;
            }
            return createData(dataType, rs);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public <T> List<T> executeQueryReturnList(String sql, Consumer<PreparedStatement> beforeExecution,
                                              Class<T> dataType) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            beforeExecution.accept(pstmt);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                return null;
            }
            return createDatas(dataType, rs);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private <T> List<T> createDatas(Class<T> dataType, ResultSet rs) throws Exception {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(createData(dataType, rs));
        }
        return result;
    }

    private <T> T createData(Class<T> dataType, ResultSet rs) throws Exception {
        Constructor<T> constructor = dataType.getDeclaredConstructor();
        constructor.setAccessible(true);
        T instance = constructor.newInstance();
        constructor.setAccessible(false);

        Field[] fields = dataType.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = rs.getObject(field.getName(), field.getType());
            field.set(instance, value);
            field.setAccessible(false);
        }
        return instance;
    }
}
