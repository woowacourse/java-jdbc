package com.interface21.jdbc.core;

/**
 * NamedParameterParser의 파싱 결과를 담는 객체
 */
public record ParsedSql(
        String executableSql,
        Object[] args
) {
}


