package com.interface21.jdbc.core.querybuilder.insert;

import com.interface21.jdbc.core.JdbcTemplate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * INSERT 쿼리를 빌드하고 실행하는 빌더 클래스입니다.
 * value() 메서드로 컬럼과 값을 추가할 수 있으며,
 * execute()를 호출하면 최종 INSERT 쿼리를 실행합니다.
 */
public class InsertQueryBuilder implements InsertValueStep, InsertExecutableStep {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final Map<String, Object> values = new LinkedHashMap<>();

    /**
     * JdbcTemplate과 테이블명을 받아 초기화합니다.
     *
     * @param jdbcTemplate 쿼리 실행에 사용할 JdbcTemplate
     * @param tableName INSERT할 테이블명
     */
    public InsertQueryBuilder(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    /**
     * INSERT할 컬럼과 값을 추가합니다.
     *
     * @param column 컬럼명
     * @param value 추가할 값
     * @return 메서드 체이닝을 위한 현재 빌더 인스턴스
     */
    @Override
    public InsertExecutableStep value(String column, Object value) {
        this.values.put(column, value);
        return this;
    }

    /**
     * 추가된 컬럼과 값을 기반으로 최종 INSERT 쿼리를 실행합니다.
     * 빌드된 INSERT SQL을 실제로 실행합니다.
     */
    @Override
    public void execute() {
        if (values.isEmpty()) {
            return; // 값이 없으면 아무것도 하지 않음
        }

        // Map의 Key로부터 컬럼 목록을 동적으로 생성
        String columns = String.join(", ", values.keySet());

        // 값의 개수만큼 placeholder(?)를 동적으로 생성
        String placeholders = String.join(", ", java.util.Collections.nCopies(values.size(), "?"));

        // 최종 INSERT SQL 문을 조립
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

        // Map의 Value로부터 파라미터 배열을 생성
        Object[] params = values.values().toArray();

        // 기존의 update 메서드를 호출하여 쿼리 실행
        jdbcTemplate.update(sql, params);
    }
}
