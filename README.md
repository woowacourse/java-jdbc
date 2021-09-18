# jwp-dashboard-jdbc

1. 학습테스트
    - [x] 학습테스트를 작성한다.
2. JDBC 템플릿 만들기
    - [x] 돌아가는 테스트를 만든다
    - [x] 필요한 유틸성 클래스를 만든다.
         - [x] Datasource를 받아 DB와 연결해주는 Connector
    - [ ] 중복코드를 리팩토링한다.
         - [x] SQL을 입력받는 로직 
         - [x] connection을 받아오고 close 하는 로직
         - [x] update query 처리와 insert query 처리의 중복 로직
    - [ ] 외부 모듈로 분리한다.
    