package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, ParameterSetter parameterSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Parameters parameters = parameterSetter.createParameters();
            parameters.setPreparedStatement(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new IllegalArgumentException("Cannot access database and connection or invalid sql query : " + sql);
        }
    }

    public <T> T queryForObject(String sql, ParameterSetter parameterSetter, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Parameters parameters = parameterSetter.createParameters();
            parameters.setPreparedStatement(pstmt);
            return getObject(rowMapper, pstmt);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new IllegalArgumentException("Cannot access database and connection or invalid sql query : " + sql);
        }
    }

    private static <T> T getObject(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        }
    }

    public <T> List<T> query(String sql, Parameters parameters, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            parameters.setPreparedStatement(pstmt);

            return getObjects(rowMapper, pstmt);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new IllegalArgumentException("Cannot access database and connection or invalid sql query : " + sql);
        }
    }

    private static <T> List<T> getObjects(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> objects = new ArrayList<>();
            while (rs.next()) {
                T object = rowMapper.mapRow(rs);
                objects.add(object);
            }
            return objects;
        }
    }
}
