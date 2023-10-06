# JDBC 라이브러리 구현하기

## 1단계 요구사항

- [x] 개발자가 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리 생성
- 리팩터링
    - [x] JdbcTemplate 내 리소스 관리 try-with-resources 구문으로 변경
    - [x] JdbcTemplate 내 중복 코드 제거
    - [x] Class 타입으로 바로 받을 수 있도록 JdbcTemplate 메서드 추가

## 2단계 요구사항

- [x] 자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성
    - [x] 익명 클래스
    - [x] 함수형 인터페이스
    - [x] 제네릭
    - [x] 가변 인자
    - [x] 람다
    - [x] try-with-resources
    - [x] checked vs unchecked exception
- [x] 라이브로리로 동작하도록 구현
    - [x] `Connection` 생성
    - [x] `Statement` 준비 및 실행
    - [x] `ResultSet` 생성
    - [x] 예외 처리
    - [x] 트랜잭션 관리
    - [x] `Connection`, `Statement`, `ResultSet` 객체 close 	

## 3단계 요구사항

- [ ] User 비밀번호 변경 기능 추가
  - [ ] 변경한 사람, 시간, 바뀐 비밀번호 이력 남기기
  - [ ] 원자성 보장을 위해 트랜잭션 적용
