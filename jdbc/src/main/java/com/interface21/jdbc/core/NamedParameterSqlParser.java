package com.interface21.jdbc.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NamedParameter에서 :paramName을 ?로 치환
 * 실행 가능한 SQL, 바인딩 순서대로 파라미터 배열 생성
 */
public class NamedParameterSqlParser {

    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");

    ParsedSql parse(final String sql, final Map<String, Object> params) {
        List<String> parameterNames = extractParameterNames(sql);
        String executableSql = getExecutableSql(sql);

        // 네임드 파라미터가 전혀 없으면 빈 인자 배열로 반환
        if (parameterNames.isEmpty()) {
            return new ParsedSql(executableSql, new Object[0]);
        }

        // 파라미터가 필요한데 맵이 null 이면 즉시 예외
        if (params.isEmpty()) {
            throw new IllegalArgumentException("SQL에 네임드 파라미터가 있지만 전달된 파라미터 맵이 null입니다");
        }

        Object[] args = getParameterArguments(params, parameterNames);
        return new ParsedSql(executableSql, args);
    }

    private List<String> extractParameterNames(final String sql) {
        Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(sql);
        List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private String getExecutableSql(String sql) {
        return NAMED_PARAMETER_PATTERN.matcher(sql).replaceAll("?");
    }

    private Object[] getParameterArguments(Map<String, Object> params, List<String> parameterNames) {
        Object[] args = new Object[parameterNames.size()];
        for (int i = 0; i < parameterNames.size(); i++) {
            final String name = parameterNames.get(i);
            if (!params.containsKey(name)) {
                throw new IllegalArgumentException("해당하는 파라미터 값이 없습니다: " + name);
            }
            args[i] = params.get(name);
        }
        return args;
    }
}


