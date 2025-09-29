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

    public void update(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            createPreparedStatementSetter(parameters).setParameters(pstmt);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T findByObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        ResultSet rs;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            createPreparedStatementSetter(parameters).setParameters(pstmt);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs, 1);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        ResultSet rs;
        List<T> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

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
    void setParameters(final PreparedStatement pstmt) throws SQLException;
}
