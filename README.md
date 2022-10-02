# JDBC 라이브러리 구현하기

> 나만의 JDBC 라이브러리를 만들어보자.

## 💻 기능 요구 사항

> 기존 MVC 코드를 [이전에 제가 구현한 코드](https://github.com/RIANAEH/jwp-dashboard-mvc/tree/step3) 로 변경해서 사용했습니다. 

### 1단계 - JDBC 라이브러리 구현하기

- [X] 직접 DataSource를 이용해 UserDao를 구현한다.
- [X] 반복 로직을 JdbcTemplate으로 이동 시키고, 이를 이용해 UserDao를 구현한다.
- [X] 항상 UserDaoTest의 모든 테스트 케이스는 통과해야한다.
