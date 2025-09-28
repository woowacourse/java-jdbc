package com.interface21.jdbc.core;

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

public class JdbcTemplate {

    private static final String NAMED_PARAMETER_REGEX = ":([a-zA-Z]*)";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Map<String, Object> params) {
        executeUpdate(sql, params);
    }

    public void update(String sql) {
        executeUpdate(sql, null);
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Map<String, Object> params) {
        return executeQuery(sql, mapper, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper) {
        return executeQuery(sql, mapper, null);
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Map<String, Object> params) {
        List<T> results = query(sql, mapper, params);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new RuntimeException("결과값이 1개보다 많습니다. 결과 크기: " + results.size());
        }
        return results.get(0);
    }

    private ParameterProcessResult processNamedParameters(String sql, Map<String, Object> params) {
        if (params == null) {
            return new ParameterProcessResult(sql, null);
        }
        
        List<String> parameterNames = extractParameterNames(sql);
        String executableSql = processNamedParameters(sql);
        Object[] args = parameterNames.stream()
                .map(params::get)
                .toArray();
        return new ParameterProcessResult(executableSql, args);
    }

    private List<String> extractParameterNames(String sql) {
        Pattern pattern = Pattern.compile(NAMED_PARAMETER_REGEX);
        Matcher matcher = pattern.matcher(sql);
        List<String> names = new ArrayList<>();

        //find()로 다음 매치를 찾고, 있으면 반복
        while (matcher.find()) {
            //정규식에서 ()가 붙은 부분을 그룹이라 함.
            //group(0)은 전체 매치 => 콜론이 들어감. group(1)은 첫번째 괄호 => 콜론 제외 이름만
            names.add(matcher.group(1));
        }
        return names;
    }

    private String processNamedParameters(String sql) {
        //namedParam자리를 ?로 대체. pstmt는 ?여야 실행 가능
        return sql.replaceAll(NAMED_PARAMETER_REGEX, "?");
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
            throw new RuntimeException(e);
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
