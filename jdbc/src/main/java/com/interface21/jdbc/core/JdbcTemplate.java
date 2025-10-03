package com.interface21.jdbc.core;

import com.interface21.jdbc.IncorrectResultSizeException;
import com.interface21.jdbc.DataAccessException;
import com.interface21.jdbc.ParameterBindingException;
import com.interface21.rowmapper.RowMapper;
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

    /**
     * INSERT, UPDATE, DELETE 전용 메서드
     *
     * @param sql 실행할 sql
     * @param params sql에 바인딩할 파라미터
     * @return 영향 받은 행(row) 수
     */
    public int update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParametersToPreparedStatement(params, pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception.getMessage());
        }
    }

    private void setParametersToPreparedStatement(Object[] params, PreparedStatement pstmt) {
        try {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        } catch (SQLException exception) {
            throw new ParameterBindingException(exception.getMessage());
        }
    }

    /**
     * SELECT 전용 메서드
     * 여러 행을 반환
     *
     * @param sql 실행할 sql
     * @param rowMapper sql 결과를 엔티티로 매핑해주는 함수형 인터페이스
     * @param params sql에 바인딩할 파라미터
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParametersToPreparedStatement(params, pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapResultSet(rowMapper, rs);
            }
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception.getMessage());
        }
    }

    private <T> List<T> mapResultSet(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }

    /**
     * SELECT 단건 조회 전용 메서드
     * 결과가 없을 시 null을 반환
     *
     * @param sql 실행할 sql
     * @param rowMapper sql 결과를 엔티티로 매핑해주는 함수형 인터페이스
     * @param params sql에 바인딩할 파라미터
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> queryResult = query(sql, rowMapper, params);
        if (queryResult.isEmpty()) {
            return null;
        }
        if (queryResult.size() > 1) {
            throw new IncorrectResultSizeException("검색 결과가 1개 이상입니다.");
        }
        return queryResult.getFirst();
    }
}
