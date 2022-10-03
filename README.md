# JDBC 라이브러리 구현하기

<details>
<summary>1단계 - JDBC 라이브러리 구현하기</summary>

구현 기능 목록

- [x] UserDaoTest의 모든 테스트 케이스가 통과시키기
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하도록 수정

리팩터링

- [x] try-with-resources 를 사용하도록 수정
- [ ] null 대신 Optional.empty() 를 반환하도록 수정
- [x] JdbcTemplate 중복 코드 없애기
- [ ] JdbcTemplate 예외 상황에 대한 테스트 추가
- [ ] Dao 테스트 격리 고민해보기

</details>
