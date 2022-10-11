# JDBC 라이브러리 구현하기

<details>
<summary>1단계 - JDBC 라이브러리 구현하기</summary>

구현 기능 목록

- [x] UserDaoTest의 모든 테스트 케이스가 통과시키기
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하도록 수정

리팩터링

- [x] try-with-resources 를 사용하도록 수정
- [x] JdbcTemplate 중복 코드 없애기
- [x] JdbcTemplate 예외 상황에 대한 테스트 추가
- [x] Dao 테스트 격리 고민해보기
- [x] insert 메서드를 분리해 id 값 가져오기
- [x] update 메서드에서 update 수를 반환하도록 수정

</details>

<details>
<summary>2단계 - 리팩터링</summary>

구현 기능 목록

- [x] 템플릿 콜백 패턴을 이용해 중복 삭제

</details>

<details>
<summary>3단계 - Transaction 적용하기</summary>

구현 기능 목록

- [x] 트랜잭션 경계 설정하기
- [x] 트랜잭션 동기화 적용하기
- [x] 트랜잭션 서비스 추상화하기

</details>
