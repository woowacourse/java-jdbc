package org.springframework.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String query, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(query);

            log.debug("query : {}", query);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return pstmt.executeUpdate();
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

    public <T> T queryForObject(final String query, final Class<T> convertClass, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(query);

            log.debug("query : {}", query);

            for(int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            rs = pstmt.executeQuery();

            if (rs.next()) {
                final ResultSetMetaData metaData = rs.getMetaData();
                final int columnCount = metaData.getColumnCount();

                final Object[] params = new Object[columnCount];
                final Class<?>[] paramTypes = new Class<?>[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    params[i] = rs.getObject(i + 1);
                    paramTypes[i] = Class.forName(metaData.getColumnClassName(i + 1));
                }

                final Constructor<T> constructor = convertClass.getConstructor(paramTypes);
                return constructor.newInstance(params);
            }
            return null;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
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

    public <T> List<T> queryForList(final String query, final Class<T> convertClass, final Object... args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(query);

            log.debug("query : {}", query);

            for(int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            rs = pstmt.executeQuery();

            final List<T> objects = new ArrayList<>();
            while (rs.next()) {
                final ResultSetMetaData metaData = rs.getMetaData();
                final int columnCount = metaData.getColumnCount();

                final Object[] params = new Object[columnCount];
                final Class<?>[] paramTypes = new Class<?>[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    params[i] = rs.getObject(i + 1);
                    paramTypes[i] = Class.forName(metaData.getColumnClassName(i + 1));
                }

                final Constructor<T> constructor = convertClass.getConstructor(paramTypes);
                objects.add(constructor.newInstance(params));
            }
            return objects;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
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
