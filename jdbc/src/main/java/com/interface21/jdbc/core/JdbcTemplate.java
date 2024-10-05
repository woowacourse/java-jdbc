package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeQuery(String sql, List<Object> paramList) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParams(paramList, pstmt);
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

    public Object executeQueryForObject(String sql, List<Object> paramList, Maker maker) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            setParams(paramList, pstmt);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return maker.make(rs);
            }
            return null;
        } catch (SQLException e) {
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

    public List<Object> executeQueryForObjects(String sql, List<Object> paramList, Maker maker) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            setParams(paramList, pstmt);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            List<Object> objects = new ArrayList<>();
            if (rs.next()) {
                Object object = maker.make(rs);
                objects.add(object);
            }
            return objects;
        } catch (SQLException e) {
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

    private void setParams(List<Object> paramList, PreparedStatement pstmt) throws SQLException {
        for (int index = 1; index <= paramList.size(); index++) {
            setParam(pstmt, index, paramList.get(index - 1));
        }
    }

    private void setParam(PreparedStatement pstmt, int index, Object param) throws SQLException {
        if (param.getClass() == Long.class) {
            pstmt.setLong(index, (Long) param);
            return;
        }
        if (param.getClass() == String.class) {
            pstmt.setString(index, (String) param);
            return;
        }
        if (param.getClass() == Date.class) {
            pstmt.setDate(index, (Date) param);
            return;
        }
        if (param.getClass() == Time.class) {
            pstmt.setTime(index, (Time) param);
            return;
        }
        if (param.getClass() == Integer.class) {
            pstmt.setInt(index, (Integer) param);
        }
    }
}
