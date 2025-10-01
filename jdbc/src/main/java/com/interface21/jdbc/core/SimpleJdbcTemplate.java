package com.interface21.jdbc.core;

import com.interface21.jdbc.core.exception.DataAccessException;
import com.interface21.jdbc.core.exception.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public class SimpleJdbcTemplate {

    private static final String NAMED_PARAMETER_REGEX = ":([a-zA-Z]*)";

    private final DataSource dataSource;

    public SimpleJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void updateWithParam(String sql, Map<String, Object> params) {
        executeUpdate(sql, params);
    }

    public void update(String sql) {
        executeUpdate(sql, null);
    }

    public <T> List<T> queryWithParam(String sql, RowMapper<T> mapper, Map<String, Object> params) {
        return executeQuery(sql, mapper, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper) {
        return executeQuery(sql, mapper, null);
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Map<String, Object> params) {
        List<T> results = queryWithParam(sql, mapper, params);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("결과값이 1개보다 많습니다. 결과 크기: " + results.size());
        }
        return results.get(0);
    }

    private ParameterProcessResult processNamedParameters(String sql, Map<String, Object> params) {
        if (params == null) {
            return new ParameterProcessResult(sql, null);
        }

        List<String> parameterNames = extractParameterNames(sql);
        String executableSql = sql.replaceAll(NAMED_PARAMETER_REGEX, "?");
        Object[] args = parameterNames.stream()
                .map(params::get)
                .toArray();
        return new ParameterProcessResult(executableSql, args);
    }

    private List<String> extractParameterNames(String sql) {
        Pattern pattern = Pattern.compile(NAMED_PARAMETER_REGEX);
        Matcher matcher = pattern.matcher(sql);
        List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private void executeUpdate(String sql, Map<String, Object> params) {
        var result = processNamedParameters(sql, params);

        executeWithPreparedStatement(result.executableSql(), result.args(), pstmt -> {
            pstmt.executeUpdate();
            return null;
        });
    }

    private <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Map<String, Object> params) {
        var result = processNamedParameters(sql, params);

        return executeWithPreparedStatement(result.executableSql(), result.args(), pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return getQueryResult(mapper, rs);
            }
        });
    }

    private <T> T executeWithPreparedStatement(String sql, Object[] args, SqlExecutor<T> executor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (args != null) {
                setParams(pstmt, args);
            }
            return executor.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("JDBC 작업 중 오류 발생", e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> getQueryResult(RowMapper<T> mapper, ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapper.mapRow(rs));
        }
        return results;
    }
}
