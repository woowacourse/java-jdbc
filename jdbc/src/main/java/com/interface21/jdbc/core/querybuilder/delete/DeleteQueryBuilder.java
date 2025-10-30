package com.interface21.jdbc.core.querybuilder.delete;// DeleteQueryBuilder.java

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DELETE 쿼리를 빌드하고 실행하는 빌더 클래스입니다.
 * where() 메서드로 WHERE 조건을 추가할 수 있으며,
 * execute()를 호출하면 최종 DELETE 쿼리를 실행합니다.
 */
public class DeleteQueryBuilder implements DeleteWhereStep, DeleteExecutableStep {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final Map<String, Object> whereClauses = new LinkedHashMap<>();

    public DeleteQueryBuilder(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    /**
     * WHERE 조건을 추가합니다. 여러 번 호출하여 AND 조건으로 연결할 수 있습니다.
     * @param column 컬럼명
     * @param value 조건 값
     * @return 메서드 체이닝을 위한 현재 빌더 인스턴스
     */
    @Override
    public DeleteExecutableStep where(String column, Object value) {
        whereClauses.put(column, value);
        return this;
    }

    /**
     * 최종 DELETE SQL을 생성하고 실행합니다.
     * 만약 where절이 하나도 없으면, 안전을 위해 아무 동작도 하지 않습니다.
     */
    @Override
    public void execute(Connection connection) {
        if (whereClauses.isEmpty()) {
            throw new DataAccessException("WHERE절 없는 DELETE는 허용되지 않습니다.");
        }

        // WHERE 절 SQL 생성
        String whereClause = whereClauses.keySet().stream()
                .map(key -> key + " = ?")
                .collect(Collectors.joining(" AND "));

        // 최종 DELETE SQL 조립
        String sql = String.format("DELETE FROM %s WHERE %s", tableName, whereClause);

        // 파라미터 배열 생성
        Object[] params = whereClauses.values().toArray();

        // JdbcTemplate의 update 메서드 호출
        jdbcTemplate.update(connection, sql, params);
    }
}
