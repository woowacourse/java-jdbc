package com.interface21.jdbc.core.querybuilder.update;

import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UPDATE 쿼리를 빌드하고 실행하는 빌더 클래스입니다.
 * set() 메서드로 SET 절을, where() 메서드로 WHERE 조건을 추가할 수 있습니다.
 * execute()를 호출하면 최종 UPDATE 쿼리를 실행합니다.
 */
public class UpdateQueryBuilder implements UpdateSetStep, UpdateExecutableStep {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final Map<String, Object> setValues = new LinkedHashMap<>();
    private final Map<String, Object> whereClauses = new LinkedHashMap<>();

    public UpdateQueryBuilder(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    /**
     * SET 절에 컬럼과 값을 추가합니다.
     * 여러 번 호출하여 여러 컬럼을 추가할 수 있습니다.
     * @param column 컬럼명
     * @param value 변경할 값
     * @return 메서드 체이닝을 위한 현재 빌더 인스턴스
     */
    @Override
    public UpdateExecutableStep set(String column, Object value) {
        setValues.put(column, value);
        return this;
    }

    /**
     * WHERE 조건을 추가합니다. 여러 번 호출하여 AND 조건으로 연결할 수 있습니다.
     * @param column 컬럼명
     * @param value 조건 값
     * @return 메서드 체이닝을 위한 현재 빌더 인스턴스
     */
    @Override
    public UpdateExecutableStep where(String column, Object value) {
        whereClauses.put(column, value);
        return this;
    }

    /**
     * SET & WHERE 절을 조립하여 최종 UPDATE 쿼리를 실행합니다.
     * 만약 set값이 하나도 없으면 아무 동작도 하지 않습니다.
     */
    @Override
    public void execute() {
        if (setValues.isEmpty()) {
            return;
        }

        // SET 절 SQL 생성
        String setClause = setValues.keySet().stream()
                .map(key -> key + " = ?")
                .collect(Collectors.joining(", "));

        // 파라미터 리스트 생성 (SET -> WHERE 순서)
        List<Object> params = new ArrayList<>(setValues.values());

        String sql = String.format("UPDATE %s SET %s", tableName, setClause);

        // WHERE 절 추가
        if (!whereClauses.isEmpty()) {
            String whereClause = whereClauses.keySet().stream()
                    .map(key -> key + " = ?")
                    .collect(Collectors.joining(" AND "));
            sql += " WHERE " + whereClause;
            params.addAll(whereClauses.values());
        }

        jdbcTemplate.update(sql, params.toArray());
    }
}
