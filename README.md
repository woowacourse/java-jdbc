# JDBC 라이브러리 구현하기

## 1단계 체크리스트

- [x] UserDaoTest의 모든 테스트 케이스가 통과한다. 
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

## 2단계 체크리스트

- [x] 최대한의 리팩터링을 한다.

### 생각 정리

- RowMapper와 JdbcTemplate, ResultSet에 rowNum에 대해서
  - 우선 RowMapper에 rowNum을 제거한 이유는 당장 필요하지않았고 앞으로도 제가 생각하는 선에서는 필요가 없다고 느꼈습니다.
  - [스프링 프레임워크 이슈 글](https://github.com/spring-projects/spring-framework/issues/7796)에서 다른 개발자분들도 RowMapper는 인터페이스이기 때문에 어떤 식으로 구현될 지 모른다. 확장을 위해 추가해 두는게 좋아보인다는 느낌으로 되어있는 것 같습니다.
  - JdbcTemplate에서 rowNum을 활용한다면 rs의 개수를 확인한다거나 특정 번째 row를 가져오고 싶을 때 사용할 수 있을 것 같습니다.
  - ResultSet에서는 데이터의 row를 포인터처럼 가르키고있고 rs.next(), rs.first(), rs.last() 등으로 이동할 수 있다고 알고있습니다.
    
    ```
    rs.last();
    int count = rs.getRow();
    ```
    와 같이 사용하여 가져올 수도 있을 것 같고
    
    ```
    rs.absolute(rowNum);
    ```
    으로 특정 번째 row로 이동하여 데이터를 받아올 수도 있을 것 같습니다.

## 3단계 체크리스트 

- [ ] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [ ] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
