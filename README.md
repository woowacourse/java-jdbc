# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기

### 학습목표
- JDBC 라이브러리를 구현하는 경험을 함으로써 중복을 제거하는 연습을 한다.
- Transaction 적용을 위해 알아야할 개념을 이해한다.

### 시작 가이드
1. 이전 미션에서 진행한 코드를 사용하고 싶다면, 마이그레이션 작업을 진행합니다.
    - 학습 테스트는 강의 시간에 풀어봅시다.
2. LMS의 1단계 미션부터 진행합니다.

## 준비 사항
- 강의 시작 전에 docker를 설치해주세요.

## 학습 테스트
1. [ConnectionPool](study/src/test/java/connectionpool)
2. [Transaction](study/src/test/java/transaction)


# 3️⃣ step3
- [x] 트랜잭션 적용
  - 원자성(Atomic) 보장
  - 트랜잭션 경계 설정
  - 비즈니스 로직이 끝나면 트랜잭션 커밋 or 롤백 실행

# 4️⃣ step4
- [ ] 트랜잭션 동기화(Transaction Synchronization)를 적용한다
  - `Connection` 객체를 따로 보관하고, DAO에서 호출할 때 저장된 커넥션을 사용한다
  - `DataSourceUtils`와 `TransactionSynchronizationManager`를 활용하여 DAO가 Connection 객체를 파라미터로 전달받아 사용하지 않도록
- [ ] 트랜잭션 서비스 추상화
  - 인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리
