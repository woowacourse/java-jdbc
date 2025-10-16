### 자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.

- [ ]  익명 클래스
- [x]  함수형 인터페이스
- [x]  제네릭
- [ ]  가변 인자
- [x]  람다
- [x]  try-with-resources
- [x]  checked vs unchecked exception

## 역할 분리

- 쿼리를 작성하고 파라미터를 세팅하고, 객체 매핑하는 작업 → DAO
- 전달받은 statement를 실행하는 작업, 전달받은 mapper를 실행하는 작업 → JdbcTemplate

## `PreparedStatementSetter` 를 통해 개선한 점

문제점:

- sql과 parameter 배열이 분리되어 있어서, 책임이 분산됐음. preparedStatement를 완성하는 로직이 dao와 jdbcTemplate에 분산되어 있었음.

개선점 → 전략패턴(callback) 사용

- dao에 preparedStatement를 완성시키는 책임을 부여한다. 코드 응집도가 올라갔다.
- 파라미터를 빼먹는 실수를 하기 어려워졌다! (개인적 생각)

## `RowMapper`를 통해 개선한 점

- 코드 양이 압도적으로 단축됐음. 유지보수성 상승
- 생성자의 타입이 변경됐을 때, 컴파일 시점에 에러를 잡아낼 수 있다.
- DB 칼럼 순서, 필드 순서에 더 이상 구애받지 않게 됐다.
- 오버헤드가 큰 리플렉션 대신, 자바 메서드 호출로 변경해 성능이 올라갔다.

## `UnCheckedException`을 통해 개선한 점

### Checked Exception

- 컴파일러가 해당 예외를 처리하는 로직이 있는지 반드시 확인함.
- `try - catch` 를 사용하거나 `throws`를 사용해 메서드 밖으로 던져야 함

### UnChecked Exception (= Runtime Exception)

- 컴파일러가 예외 처리 로직을 확인하지 않고 넘어감

### 현재 문제

`SQLException` 은 Checked Exception이라, 라이브러리의 예외가 app 계층까지 전달됨.

그러면 모든 특정 기술에 의존하게 된다.

### 해결 방법

JDBCTemplate에서 `SQLException`을 잡고, `RuntimeException`으로 바꿔준다.