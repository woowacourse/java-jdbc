# 기능 목록

## 1. JDBC 라이브러리 구현

- [x] 라이브러리 개발
    - 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중한다.
    - 리팩터링은 UserDaoTest를 활용해 진행한다.
    - 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
    - DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출하면 된다.

# 2. 리팩터링

- 단계적으로 클린 코드 작성

- [ ] [1] 메서드 추출
    - UserDao update와 insert 메서드에서 사용자가 수정해야 되는 부분을 메서드로 분리한다.
- [ ] [2] 클래스 추출
    - update 메서드와 관련된 코드를 UpdateJdbcTemplate 클래스를 만들어 이동시킨다.
    - insert도 마찬가지로 InsertJdbcTemplate 클래스를 만들어 이동한다.
    - UserDao에서 update, insert 각 메서드는 코드를 이동한 클래스를 사용하도록 수정한다.
- [ ] [3] 템플릿 메서드 패턴 적용
    - UpdateJdbcTemplate, InsertJdbcTemplate를 하나의 클래스로 합친다.
    - 사용자가 구현해야되는 부분은 추상 메서드로 만든다.
    - 클래스명, 메서드명을 범용적으로 쓸 수 있게 수정한다.
    - UserDao도 변경한 클래스를 사용하도록 변경한다.
- [ ] [4] 도메인 의존도 제거
    - UserDao에서 추상 클래스를 객체로 만들어 user 객체에 대한 의존도를 제거한다.
- [ ] [5] 다시 한 번 클래스 추출
    - select도 SelectJdbcTemplate으로 분리한다.
- [ ] [6] 다시 한 번 템플릿 메서드 패턴 적용
    - SelectJdbcTemplate, JdbcTemplate를 하나의 클래스로 합친다.
- [ ] [7] 불필요한 mapRow 메서드 제거
    - mapRow, setValues를 인터페이스로 분리하여 불필요한 구현을 제거한다.
- [ ] [8] 라이브러리 확장
    - 커스텀 Exception을 추가해 사용자는 Unchecked Exception이 되도록 변경한다.
    - RowMapper 인터페이스에서 제네릭을 사용하여 개선한다.
    - 람다를 활용해서 PreparedStatementSetter 인터페이스를 구현한다.
