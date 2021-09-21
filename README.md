# jwp-dashboard-jdbc

## 미션 내용 
- JDBCTemplate을 직접 구현하여 SQL Connection 생성 및 DB 쿼리를 라이브러리화 한다. 
- APP 모듈에 비지니스 관련 로직만 남고 이외의 DB 관련 로직은 JDBC 모듈로 분리한다. 
- 코드가 간결해지도록 점진적인 리팩토링을 경험한다. 
- 라이브러리의 확정성을 고려하면서 로직을 설계한다. 
    - 가변인자, 템플릿메소드패턴, 함수형 인터페이스 등 활용

## 요구사항 
-[x] `UserDao`에 있는 DB 쿼리 관련 메서드 구현 
    - 'UserDaoTest' 가 돌아가도록 우선 구현
    
- [x] `JDBCTemplate`으로 중복 코드 분리
    - [x] Connection 생성 
    - [x] PreparedStatement 쿼리 할당 및 실행 
    - [x] ResultSet 생성
    - [x] rowMapper로 ResultSet -> Object 변환 및 반환
  
- [x] 필요 `JDBCTemplate` 라이브러리 메서드 
  - [x] Insert 쿼리 
  - [x] Select 쿼리 
    
- [x] 예외 처리 
    - [x] RuntimeException 으로 checked -> unchecked로 변경
    
- [ ] Transaction 관리
- [x] 사용한 리소스(connection, resultSet, preparedStatement) close


- [ ] 추가 리팩토링
    - [ ] application.properties에 DB 관련 정보 저장하여 app 모듈에 DataSource 코드 제거
    - [x] `resultSet`에서 `rowMapper`나 `queryForObject` 사용 시 객체 생성 및 반환
    

## 고민할 점 
- setString, setLong vs. setObject in preparedStatement

## 학습 내용
