# JDBC 라이브러리 구현하기 미션

# 1단계 

## 체크 사항
- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

## 기능 요구사항
- 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이블러리르 직접 만들자

## 리펙터링 과정

### 기존의 코드를 템플릿 콜백 패턴으로 변경

- `try-catch, 자원 반납`을 Dao를 작성하는 클라이언트가 모르게끔 코드를 변경.

[템플릿 콜백 패턴 리펙터링 코드](docs/step1_refactor_1_template_callback.md)

### JdbcTemplate으로 코드 변경

[JdbcTemplate 리펙터링 코드](docs/step1_refactor_2_jdbc_template.md)
