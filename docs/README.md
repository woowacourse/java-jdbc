# 구현 기능 목록

## 1단계 JDBC 라이브러리 구현
- [x] UserDao에 있는 코드 JdbcTemplate으로 이동
- [x] UserDao 동작하도록 구현

## 2단계 
- [x] 예외처리 코드 작성
  - [x] 쿼리 조회 결과가 2개 이상이거나 없을 경우 예외 처리
  - [x] RuntimeException 대신 구체적 예외 처리
- [x] JDBC 탬플릿의 중복된 try catch문 제거

## 3단계
- [x] 매튜 리뷰 반영
  - [x] JdbcTemplate 변수 네이밍 명확하게 수정
  - [x] 사용하지 않는 log 제거

- [x] User에 비밀번호 변경 기능 구현
  - [x] 변경 이력 저장
  - [x] 트랜잭션 설정

## 4단계
- [ ] Transaction synchronization 적용
- [ ] 트랜잭션 서비스 추상화하기
