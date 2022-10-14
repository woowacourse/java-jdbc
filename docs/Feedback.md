# 리뷰어 피드백 정리

## Level 1 - JDBC 라이브러리 구현

- [x] SpringFramework 의존성 제거
    - 사용 위치
        - JdbcTemplate, EmptyResultDataAccessException
        - JdbcTemplate, IncorrectResultSizeDataAccessException
        - JdbcTemplate, RowMapper
- [x] RowMapperResultSetExtractor 클래스 분리 이유
- [x] JdbcTemplate, DataAccessException 활용

## Level 2 - 리팩터링

- [ ] getSingleResult 네이밍 수정
    - 이름으로는 하나의 결과값을 반환한다고 생각할 수 있지만 결과 반환과 함께 검증 로직까지 포함하고 있음.

## Level 3 - Transaction 적용하

- [x] UserDao, 불필요한 import 제거
- [x] Connection에 대한 테스트 실패 수정
- [ ] userService.changePassword() 예외 발생 시 처리 
