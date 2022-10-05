# JDBC 라이브러리 구현하기

> 나만의 JDBC 라이브러리를 만들어보자.

## 💻 기능 요구 사항

> 기존 MVC 코드를 [이전에 제가 구현한 코드](https://github.com/RIANAEH/jwp-dashboard-mvc/tree/step3) 로 변경해서 사용했습니다. 

### 1단계 - JDBC 라이브러리 구현하기

- [X] 직접 DataSource를 이용해 UserDao를 구현한다.
- [X] 반복 로직을 JdbcTemplate으로 이동 시키고, 이를 이용해 UserDao를 구현한다.
- [X] 항상 UserDaoTest의 모든 테스트 케이스는 통과해야한다.

## 🧹 JdbcTemplate

- 단위 테스트를 추가해 내부 동작이 의도에 맞게 동작하는지 확인했다. 

### update()

- PreparedStatement의 executeUpdate()를 래핑한다.
- 기존 executeUpdate()의 반환값을 그대로 반환한다.

### query()

- PreparedStatement의 executeQuery()를 래핑한다.
- RowMapper를 활용해 executeQuery()에서 반환하는 ResultSet을 개발자가 원하는 객체의 형태로 추출할 수 있다. 
- 결과는 List 형태이며 0개가 될 수도 있고 여러개가 될 수도 있다. 

### queryForObject()

- query()에서 반환하는 값이 무조건 1개일 때 사용한다.
- UserTest를 돌려보니 실패했다. 알고보니 메서드 별로 격리되고 있지 않았고, 현재 트랜잭션을 붙일 수 없어 메서드 마다 데이터를 삭제하고 id를 1로 초기화하도록 설정했다. 

