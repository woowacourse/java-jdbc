# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기

### 1. Transaction Synchronization

트랜잭션 관리하는 로직을 비즈니스 로직에서 분리하는 기술이다.
`Connection`객체를 현재 실행 중인 스레드에 연결 시켜두는 것이 핵심이다.

**키워드**

- `ThreadLocal`: 각 스레드마다 고유한 저장 공간을 제공하는 자바 클래스
    - A 스레드가 저장한 데이터는 B 스레드가 접근할 수 없다.

### 2. 문제 해결 방식

트랜잭션 동기화를 적용하더라돋, `UserService`가 여전히 `DataSource`에 의존적이고, `bindResource`, `unbindResourcd` 같은
트랜잭션 관리 로직을 직접 수행해야 하는 문제가 남았음.

이 문제를 해결하기 위해 `UserService`를 Interface로 만들고, 비즈니스 구현체와 트랜잭션 구현체를 구분함.

1. `UserService` **(Interface)**
    - 클라이언트가 의존하는 공통 인터페이스
    - `findById`, `changePassword` 등 메서드를 정의함.

2. `AppUserService` **(핵심 로직)**
    - `UserService`인터페이스의 구현체다.
    - `Connection`, `DataSourcd`, `Transaction`의 존재를 알지 못함.
    - 오직 `UserDao`, `UserHistoryDao`를 호출하는 비즈니스 로직만 담당함!

3. `TransactionUserService`
    - `UserServic` 인터페이스의 트랜잭션 구현체다.
    - `chnagePassword`와 같이 트랜잭션이 필요한 메서드는 직접 처리하고, `findById`처럼 필요 없는 메서드는 원본 객체에 그대로 위임한다.

### 3. `TransactionUserService`의 작동 방식

1. 트랜잭션 시작
2. 커넥션 바인딩
3. 핵심 로직 위임
4. 트랜잭션 완료
5. 리소스 정리 (풀 반납, 스레드 로컬 정리)

### 4. `JdbcTemplate` 수정 사항

`AppUserService`는 `Connection`을 모르지만, 그 내부의 `JdbcTemplate`는 트랜잭션에 참여해야함!
`DataSourceUtils`로 해결이 됨!

기존에 `dataSource.getConnection()`을 직접 호출하던 로직을 `DataSourceUtils.getConnection(dataSource)`로 변경했습니다.
그리고, `Connection`을 닫던 로직을 `finally`에서 `DataSource.releaseConnection`로 변경했습니다.

**`DataSourceUtils`의 역할**

- `getConnection`은 `ThreadLocal`을 확인하는데, 트랜잭션 중이라면 저장해 둔 커넥션을 반환하고, 그렇지 않다면 새로운 커넥션을 반환합니다.
- `releaseConnection`은 `ThreadLocal`을 확인하는데, 트랜잭션 중이라면 커넥션을 닫지 않고, 트랜잭션 밖이라면 커넥션을 닫습니다.
    - 트랜잭션 매니저인 `TransactionUserService`가 닫도록 했습니다! 왜냐하면, 그 트랜잭션이 언제 완전히 끝나는지 아는 애는 트랜잭션 매니저니까입니다.
    - `DataSourceUtils`는 트랜잭션의 주인이 아니니까!
