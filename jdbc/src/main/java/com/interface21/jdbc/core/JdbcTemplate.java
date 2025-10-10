package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public DataSource getDataSource() {
        return dataSource;
    }

    public int update(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            createPreparedStatementSetter(parameters).setValues(pstmt);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            log.info("Fail update. sql={}, parameters={}", sql, parameters);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryByObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> list = query(sql, rowMapper, parameters);
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        ResultSet rs;
        List<T> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            createPreparedStatementSetter(parameters).setValues(pstmt);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... parameters) throws SQLException {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        };
    }
}

interface PreparedStatementSetter {
    void setValues(final PreparedStatement pstmt) throws SQLException;
}
