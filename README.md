# JDBC 라이브러리 구현하기

## 요구사항 체크리스트

### 1단계 - JDBC 라이브러리 구현하기

- [x] UserDao를 구현한다.
  - [x] findAll 메서드를 구현한다.
  - [x] findByAccount 메서드를 구현한다.
  - [x] update 메서드를 구현한다.
  - [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.


### 2단계 - 리팩터링
- [x] 템플릿 메서드 패턴 적용
- [x] 콜백 적용
- [x] 불필요한 구현을 제거하기 위해 mapRow, setValues를 인터페이스로 분리

### 3단계 - Transaction 적용하기
- [x] 트랜잭션 경계 설정 - userDao와 userHistoryDao가 동일한 Connection 객체를 사용하도록 변경 
- [ ] 트랜잭션 동기화 적용 - PlatformTransactionManager 인터페이스를 활용하여 DAO가 Connection 객체를 파라미터로 전달받아 사용하지 않도록 변경
- [ ] 트랜잭션 서비스 추상화 - 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리
