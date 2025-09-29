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

    // TODO. 현재는 반환값을 User에 대해서만, 나중에 전역적인 Object
    public <T> T findByObject(String sql, RowMapper<T> rowMapper, Object object) { // TODO. 현재는 하나의 인자에 대해서만, 나중에 args
        ResultSet rs;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setObject(1, object);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs, 1);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO. 추후 네이밍 수정
    public <T> List<T> findAll(String sql, RowMapper<T> rowMapper) {
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
}
