package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public abstract class JdbcTemplate<T> {

    private static final String NAMED_PARAMETER_REGEX = ":([a-zA-Z]*)";

    public void update(final T object) {
        final String sql = createQuery();

        final Map<String, Object> params = new HashMap<>();
        setValues(object, params);

        final ParameterProcessResult result = processNamedParameters(sql, params);
        final String executableSql = result.executableSql();
        final Object[] args = result.args();

        executeWithPreparedStatement(executableSql, args, pstmt -> {
            pstmt.executeUpdate();
            return null;
        });
    }

    public <R> List<R> query(final String sql, final RowMapper<R> rowMapper) {
        return query(sql, rowMapper, new HashMap<>());
    }

    public <R> List<R> query(final String sql, final RowMapper<R> rowMapper, final Map<String, Object> params) {
        final ParameterProcessResult result = processNamedParameters(sql, params);
        final String executableSql = result.executableSql();
        final Object[] args = result.args();

        return executeWithPreparedStatement(executableSql, args, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                return getQueryResult(rowMapper, rs);
            }
        });
    }

    public <R> R queryForObject(final String sql, final RowMapper<R> rowMapper, final Map<String, Object> params) {
        final List<R> results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    protected abstract String createQuery();

    protected abstract void setValues(T object, Map<String, Object> params);

    protected abstract DataSource getDataSource();

    private ParameterProcessResult processNamedParameters(final String sql, final Map<String, Object> params) {
        if (params == null) {
            return new ParameterProcessResult(sql, null);
        }

        final List<String> parameterNames = extractParameterNames(sql);
        final String executableSql = sql.replaceAll(NAMED_PARAMETER_REGEX, "?");
        final Object[] args = parameterNames.stream()
                .map(params::get)
                .toArray();
        return new ParameterProcessResult(executableSql, args);
    }

    private List<String> extractParameterNames(final String sql) {
        final Pattern pattern = Pattern.compile(NAMED_PARAMETER_REGEX);
        final Matcher matcher = pattern.matcher(sql);
        final List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private void setParams(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <R> R executeWithPreparedStatement(final String sql, final Object[] args, final SqlExecutor<R> executor) {
        try (final Connection conn = getDataSource().getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (args != null) {
                setParams(pstmt, args);
            }
            return executor.execute(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <R> List<R> getQueryResult(final RowMapper<R> mapper, final ResultSet rs) throws SQLException {
        final List<R> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapper.mapRow(rs));
        }
        return results;
    }
}
