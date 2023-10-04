# JDBC 라이브러리 구현하기

## 1단계 - JDBC 라이브러리 구현하기

- [x] JDBC Template 구현
    - [x] 데이터 변경 쿼리에 대한 메서드
    - [x] 단일 객체 조회 쿼리에 대한 메서드
    - [x] 복수 객체 조회 쿼리에 대한 메서드

- [x] UserDao 리팩터링

## 2단계 - 리팩터링

- [x] Jdbc Template 중복코드 제거
    - [x] Connection
    - [x] PreparedStatement + setParameters
    - [x] ResultSet

- [x] 추천 도구
    - [x] 익명 클래스
    - [x] 함수형 인터페이스
    - [x] 제네릭
    - [x] 가변인자
    - [x] 람다
    - [x] try-with-resources
    - [x] checked vs unchecked exception

- [x] 변하는 요소에 대한 추상화
    - [x] PreparedStatementMaker
    - [x] PreparedStatementExecuter

- 변하는 것
    - PreparedStatement 만드는 것
    - PreparedStatement 으로 execute 하는 것
        - executeUpdate()
        - executeQuery()

- 변하지 않는 것
    - Connection, PreparedStatement 자원 열고 닫는 것
    - SQLException 에 대한 예외 처리
