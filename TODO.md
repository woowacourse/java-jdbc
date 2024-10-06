### TODO 리스트

- [ ] **단일 객체 조회 메서드 구현**
    - [ ] `queryForObject(String sql, Object[] params, Class<T> requiredType)`
        - 파라미터가 있는 SQL 쿼리 실행 후 단일 결과를 반환하는 메서드.

- [ ] **데이터 삽입 및 수정 메서드 구현**
    - [ ] `update(String sql, Object[] params)`
        - 파라미터가 있는 SQL 쿼리를 바인딩하여 `INSERT`, `UPDATE`, `DELETE` 작업을 처리하는 메서드.

- [ ] **다수 객체 조회 메서드 구현**
    - [ ] `query(String sql, RowMapper<T> rowMapper)`
        - SQL 쿼리를 실행하고 `RowMapper`를 사용하여 결과를 리스트로 변환하는 메서드.
