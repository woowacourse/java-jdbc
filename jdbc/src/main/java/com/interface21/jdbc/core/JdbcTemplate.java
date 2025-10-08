package com.interface21.jdbc.core;

import com.interface21.jdbc.mapper.Mapper;
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
    private final Mapper mapper;

    public JdbcTemplate(final DataSource dataSource, final Mapper mapper) {
        this.dataSource = dataSource;
        this.mapper = mapper;
    }

    // try - with - resources 로 자원 반환을 알아서 하게 설정
    // 이 메서드는 update(insert, update, delete) 에서 사용
    // 함수형 인터페이스를 사용해, 함수를 인자로 전달할 수 있게 변경
    // 전달할 함수 = 파라미터 세팅(setString, setLong ...)
    public void executeUpdate(String sql, Object[] params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParams(pstmt, params);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T executeSelect(String sql, Object[] params, Class<T> clazz) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapper.map(rs, clazz);
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> executeSelectAll(String sql, Object[] params, Class<T> clazz) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();

            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(mapper.map(rs, clazz));
            }
            return results;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object[] params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
