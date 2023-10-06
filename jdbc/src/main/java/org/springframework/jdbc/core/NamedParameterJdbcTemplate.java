package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.SQLExceptionTranslator;

public class NamedParameterJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(NamedParameterJdbcTemplate.class);

    private final DataSource dataSource;

    public NamedParameterJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Map<String, Object> parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(getOriginalSql(sql));
        ) {
            log.debug("query : {}", sql);
            setQueryParameters(sql, pstmt, parameters);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Map<String, Object> parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(getOriginalSql(sql), ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
        ) {
            setQueryParameters(sql, pstmt, parameters);
            ResultSet rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            T result = null;
            if (rs.next()) {
                result = rowMapper.mapRow(rs, rs.getRow());
            }

            verifyResultRowSize(rs, 1);

            return Optional.ofNullable(result);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(e);
        }
    }

    private static void verifyResultRowSize(final ResultSet rs, final int expectedRowSize) throws SQLException {
        rs.last();
        final int rowSize = rs.getRow();
        rs.beforeFirst();
        if (expectedRowSize != rowSize) {
            throw new SQLException(String.format("결과가 1개인 줄 알았는데, %d개 나왔서!", rowSize));
            /**
             * 예외 원문 : Incorrect result size: expected 1, actual n
             */
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Map<String, Object> parameters) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(getOriginalSql(sql));
        ) {
            setQueryParameters(sql, pstmt, parameters);

            ResultSet rs = pstmt.executeQuery();
            log.debug("query : {}", sql);

            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(e);
        }
    }

    private static void setQueryParameters(final String sql,
                                           final PreparedStatement pstmt,
                                           final Map<String, Object> parameters
    ) throws SQLException {
        final List<String> parameterNames = getQueryParameters(sql);

        for (int index = 0; index < parameterNames.size(); index++) {
            final String parameter = parameterNames.get(index);
            if (parameters.get(parameter) == null) {
                throw new DataAccessException(String.format("쿼리 파라미터를 빼먹었서 : %s", parameter));
            }
            pstmt.setObject(index + 1, parameters.get(parameter));
        }
    }

    private static List<String> getQueryParameters(final String sql) {
        Pattern pattern = Pattern.compile(":(\\w+)");
        Matcher matcher = pattern.matcher(sql);

        final List<String> queryParameters = new ArrayList<>();
        while (matcher.find()) {
            queryParameters.add(matcher.group(1)); // 그룹 1은 단어 부분을 나타냅니다.
        }

        return queryParameters;
    }


    private String getOriginalSql(final String sql) {
        return sql.replaceAll(":\\w+", "?");
    }
}
