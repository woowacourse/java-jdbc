package com.interface21.jdbc.core.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.interface21.jdbc.core.SqlParameterSource;

public class Sql {

    private static final int FIRST_GROUP_INDEX = 1;

    private final String value;

    public Sql(final String value) {
        validateSqlIsNullOrBlank(value);
        this.value = value;
    }

    private void validateSqlIsNullOrBlank(final String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql문은 null 혹은 공백이 입력될 수 없습니다. - " + sql);
        }
    }

    public Sql bindingParameters(final SqlParameterSource parameterSource) {
        validateParameterSourceIsNull(parameterSource);
        final List<String> bindingParameterNames = parseBindingParameterNames();
        String result = this.value;
        for (final String bindingParameterName : bindingParameterNames) {
            final Object parameterValue = parameterSource.getParameter(bindingParameterName);
            result = bindingParameterValue(result, bindingParameterName, parameterValue);
        }

        return new Sql(result);
    }

    private void validateParameterSourceIsNull(final SqlParameterSource parameterSource) {
        if (parameterSource == null) {
            throw new IllegalArgumentException("parameter source는 null이 입력될 수 없습니다.");
        }
    }

    private List<String> parseBindingParameterNames() {
        final String regex = ":([a-zA-Z_][a-zA-Z0-9_]*)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(this.value);
        final List<String> bindingParameterNames = new ArrayList<>();
        while (matcher.find()) {
            bindingParameterNames.add(matcher.group(FIRST_GROUP_INDEX));
        }

        return bindingParameterNames;
    }

    private String bindingParameterValue(
            final String sql,
            final String parameterName,
            final Object parameterValue
    ) {
        final String value = String.valueOf(parameterValue);
        if (parameterValue instanceof String) {
            return sql.replace(":" + parameterName, "'" + value + "'");
        }

        return sql.replace(":" + parameterName, value);
    }

    public Sql bindingParameters(final Map<String, Object> parameters) {
        validateParametersIsNullOrEmpty(parameters);
        final List<String> bindingParameterNames = parseBindingParameterNames();
        String result = this.value;
        for (final String bindingParameterName : bindingParameterNames) {
            final Object parameterValue = parameters.get(bindingParameterName);
            result = bindingParameterValue(result, bindingParameterName, parameterValue);
        }

        return new Sql(result);
    }

    private void validateParametersIsNullOrEmpty(final Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new IllegalArgumentException("parameter map은 null 혹은 빈 값이 입력될 수 없습니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
