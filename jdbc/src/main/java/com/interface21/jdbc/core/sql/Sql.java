package com.interface21.jdbc.core.sql;

import java.util.ArrayList;
import java.util.List;
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
            throw new IllegalArgumentException("sql문은 null 혹은 공백이 입력될 수 없습니다.");
        }
    }

    public Sql bindingParameters(final SqlParameterSource parameterSource) {
        final List<String> bindingParameterNames = parseBindingParameterNames();
        String result = this.value;
        for (String bindingParameterName : bindingParameterNames) {
            final String parameterValue = parameterSource.getParameterValue(bindingParameterName);
            result = replaceBindingParameterNameToValue(result, bindingParameterName, parameterValue);
        }

        return new Sql(result);
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

    private String replaceBindingParameterNameToValue(
            final String sql,
            final String bindingParameterName,
            final String bindingParameterValue
    ) {
        return sql.replace(":" + bindingParameterName, "'" + bindingParameterValue + "'");
    }

    public String getValue() {
        return this.value;
    }
}
