# JDBC 라이브러리 구현하기

## 1단계 - JDBC 라이브러리 구현하기

- [x] JDBC Template 구현
    - [x] 데이터 변경 쿼리에 대한 메서드
    - [x] 단일 객체 조회 쿼리에 대한 메서드
    - [x] 복수 객체 조회 쿼리에 대한 메서드

- [x] UserDao 리팩터링

## 2단계 - 리팩터링

- [x] Jdbc Template 중복코드 제거
    - [x] Connection
    - [x] PreparedStatement + setParameters
    - [x] ResultSet

- [x] 추천 도구
    - [x] 익명 클래스
    - [x] 함수형 인터페이스
    - [x] 제네릭
    - [x] 가변인자
    - [x] 람다
    - [x] try-with-resources
    - [x] checked vs unchecked exception

- [x] 변하는 요소에 대한 추상화
    - [x] PreparedStatementMaker
    - [x] PreparedStatementExecuter

- 변하는 것
    - PreparedStatement 만드는 것
    - PreparedStatement 으로 execute 하는 것
        - executeUpdate()
        - executeQuery()

- 변하지 않는 것
    - Connection, PreparedStatement 자원 열고 닫는 것
    - SQLException 에 대한 예외 처리

## Transaction 학습테스트

- [x] 1단계 - Isolation level
    - [x] 표 채우기

|                  | Dirty reads | Non-repeatable reads | Phantom reads |
|------------------|-------------|----------------------|---------------|
| Read Uncommitted | 발생          | 발생                   | 발생            |
| Read Committed   | 발생하지 않음     | 발생                   | 발생            |
| Repeatable Read  | 발생하지 않음     | 발생하지 않음              | 발생            |
| Serializable     | 발생하지 않음     | 발생하지 않음              | 발생하지 않음       |

- [x] 2단계 - Propagation
    - [x] 테스트 통과
      - [x] testRequired()
        - saveFirstTransactionWithRequired() 메서드는 REQUIRED 설정. 메서드 호출 시 트랜잭션 한 개 생성.
        - 내부에서 saveSecondTransactionWithRequired() 호출. 이 메서드는 REQUIRED 설정. 기존 트랜잭션 이어서 사용.
        - 총 1개 트랜잭션.
      - [x] testRequiredNew()
        - saveFirstTransactionWithRequiredNew() 메서드는 REQUIRED 설정. 메서드 호출 시 트랜잭션 한 개 생성.
        - 내부에서 saveSecondTransactionWithRequiresNew() 호출. 이 메서드는 REQUIRES_NEW 설정. 메서드 호출 시 트랜잭션 한 개 생성.
        - 총 2개 트랜잭션.
      - [x] testRequiredNewWithRollback()
        - saveSecondTransactionWithRequiresNew() 메서드가 REQUIRES_NEW 설정이기 때문에, 이 메서드의 동작에 saveAndExceptionWithRequiredNew() 메서드의 트랜잭션이 영향을 주지 않는다.
        - 따라서 saveAndExceptionWithRequiredNew() 메서드가 롤백되더라도, User가 save 된다.
      - [x] testSupports()
      - [x] testMandatory()
      - [x] testNotSupported()
      - [x] testNested()
      - [x] testNever()

## 3단계 - Transaction 적용

- [x] UserService Transaction 적용
    - [x] UserDao, UserHistoryDao 같은 connection 사용하도록 설정

## 4단계 - Transaction synchronization 적용

- [ ] Transaction synchronization 적용
  - [ ] DataSourceUtils 사용
  - [ ] TransactionSynchronizationManager 구현

- [ ] Transaction Service 추상화
  - [ ] Transaction을 다루는 Service 따로 만들기
  - [ ] testTransactionRollback() 테스트 통과
