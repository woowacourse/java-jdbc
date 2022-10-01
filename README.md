# JDBC 라이브러리 구현하기

## 1단계 - JDBC 라이브러리 구현하기

- **기능 요구사항**
    - 개발자는 SQL 쿼리 작성, 쿼리에 전달한 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.
- **체크리스트**
    - [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
    - [ ] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

### JdbcTemplate 구현

|                       | statement 값 지정 | resultSet 필요 | return type  |
|----------------------|:--------------:|:------------:|:------------:|
|      insert(User)     |       ✅        |      ❌       |    `void`    |
|      update(User)     |      ✅      |      ❌      |    `void`     |
|       findAll()       |      ❌     |      ✅       | `List<User>` |
|     findById(Long)    |       ✅     |      ✅       |    `User`    |
| findByAccount(String) |      ✅      |      ✅       |    `User`    |

- **메서드 공통**
    - `DataSource`를 이용하여 `Connection`을 연결한다.
    - `Connection`을 이용하여 `Statement`를 얻는다.
    - 특징에 맞게 세부사항을 구현한다.

- [ ] `execute`를 구현한다(insert, update) : param 값은 지정할 수 있지만 resultSet이 따로 필요없는 경우
    - param을 설정할 수 있어야한다.
        - [ ] Map을 활용한 방식으로 구현해본다.
    - return으로는 뭘 줄까? -> 일단 return type이 필요없으므로 `void`.. 더 고민해보자
