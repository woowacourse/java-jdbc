# JdbcTemplate

- `package org.springframework.jdbc.core;`
- JDBC 코어 패키지의 중심에 해당하는 클래스
- SQL문과 결과를 추출하는 코드를 애플리케이션 구현 영역으로 남기고, 여기선 JDBC 워크플로우를 수행.
- SQL 쿼리 또는 업데이트를 수행한다. CUD와 R을 분리해서 표현하고 있음. query, update
- JDBC 예외를 포괄적이고 더 구체적인 정보를 가진 예외로 변환(Translation)한다.
    - 변환하는 예외들은 org.springframework.dao 패키지에 정의되어 있다.
- JdbcTemplate을 사용하는 코드는 콜백 인터페이스만 구현하면 된다.
- PreparedStatementCreator 콜백. Connection을 받고, SQL과 파라미터를 제공한다.
- ResultSetExtractor. ResultSet으로부터 값을 추출한다.
- PreparedStatementSetter, RowMapper. 대체 콜백 인터페이스들
- 이 클래스에선 모든 로그를 디버그 레벨로 기록한다.
- 생성자 → getter/setter → 메서드 순으로 정렬했다

<br><br>

# ConnectionCallback<T>

```java

@FunctionalInterface
public interface ConnectionCallback<T> {

    @Nullable
    T doInConnection(Connection con) throws SQLException, DataAccessException;
}
```

```java
@Override
@Nullable
public<T> T execute(ConnectionCallback<T> action)throws DataAccessException{
        Assert.notNull(action,"Callback object must not be null");

        Connection con=DataSourceUtils.getConnection(obtainDataSource());
        try{
        // Create close-suppressing Connection proxy, also preparing returned Statements.
        Connection conToUse=createConnectionProxy(con);
        return action.doInConnection(conToUse);
        }
        catch(SQLException ex){
        // Release Connection early, to avoid potential connection pool deadlock
        // in the case when the exception translator hasn't been initialized yet.
        String sql=getSql(action);
        DataSourceUtils.releaseConnection(con,getDataSource());
        con=null;
        throw translateException("ConnectionCallback",sql,ex);
        }
        finally{
        DataSourceUtils.releaseConnection(con,getDataSource());
        }
        }
```

- JdbcTemplate 의 execute 메서드에 전달할 매개변수
- Connection 을 받아서 수행할 내용을 작성해서 전달하는 데 사용된다
- SQL 예외를 던지기 때문에 클라이언트의 사용성이 증대된다.
- 단, 이 보다 근래에 작성된 JdbcTemplate의 query, update 메서드를 사용하라고 강력히 권고하고 있다.
- JdbcTemplate의 개발 초기에 제공되었던 인터페이스이나 현재는 권장되지 않는 사용방식이다.
- 트랜잭션 없이 ResultSet까지 클라이언트가 직접 핸들링하던 방식으로 사용할 때에만 사용가능한 듯 하다.
- JdbcTemplate 내에서도 ConnectionCallback 이 사용되는 메서드는 단 하나인데, 이 마저도 하위 호환성을 위해 남겨둔 듯 하다. 초기엔 이런 게 있었구나 하고 넘어가면 될 듯 하다.
- 단 하나의 그 메서드는 이것이다.
    - `public <T> T execute(ConnectionCallback<T> action) throws DataAccessException`

<br><br>

# StatementCallback<T>

```java

@FunctionalInterface
public interface StatementCallback<T> {
    @Nullable
    T doInStatement(Statement stmt) throws SQLException, DataAccessException;
}
```

```java
@Nullable
private<T> T execute(StatementCallback<T> action,boolean closeResources)throws DataAccessException{
        Connection con=DataSourceUtils.getConnection(obtainDataSource());
        Statement stmt=null;
        try{
        stmt=con.createStatement();
        T result=action.doInStatement(stmt);
        return result;
        }
        catch(SQLException ex){
        // Statement, Connection을 Close하고 예외를 Translate해서 던진다.
        }finally{
        // closeResources가 true라면 Statement와 Connection을 Close한다.
        }
        }
```

- 앞서 살펴본 ConnectionCallback은 Connection을 받아서 처리할 내용을 구현하는 것이었다.
- StatementCallback은 커넥션을 가져오고, Statement를 생성하는 부분까지 템플릿화했다.
- 변경되는 부분은 Statement를 받아서 사용하는 부분으로 더 줄였다.
- StatementCallback과 boolean을 매개변수로 받는 private execute 메서드가 핵심 메서드들에서 재사용되는 템플릿 메서드이다.
    - 즉, query, update, batchUpdate 등 핵심 메서드에서 변하는 코드를 구현해서 private execute 메서드를 호출하는 식이다.
- queryForStream 메서드에서만 execute의 boolean 매개변수를 false로 전달하고 그 외엔 모두 true로 전달한다.
- 아직까진 RowMapper도 PreparedStatement도 안보이는데?? 어떻게 된걸까… 이제 query, update를 살펴보자!!

<br><br>

# JdbcTemplate#query

```java
@Override
@Nullable
public<T> T query(final String sql,final ResultSetExtractor<T> rse)throws DataAccessException{

class QueryStatementCallback implements StatementCallback<T>, SqlProvider {
    @Override
    @Nullable
    public T doInStatement(Statement stmt) throws SQLException {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            return rse.extractData(rs);
        } finally {
            JdbcUtils.closeResultSet(rs);
        }
    }

    @Override
    public String getSql() {
        return sql;
    }
}

    return execute(new QueryStatementCallback(),true);
            }
```

```java

@FunctionalInterface
public interface ResultSetExtractor<T> {
    @Nullable
    T extractData(ResultSet rs) throws SQLException, DataAccessException;
}
```

- 조회에 사용되는 query메서드는 String sql, ResultSetExtractor<T> rse 두 개를 매개변수로 받는다.
- Statement를 받아 T를 리턴하는 StatementCallback과 String sql을 반환하는 getSql을 요구하는 SqlProvider 두 가지 인터페이스를 구현한 클래스를 메서드 내부에 선언한다.
- ResultSetExtractor 는 ResultSet을 받아 T를 반환하는 함수형 인터페이스이다.
- query메서드는 Sql 과 RowMapper 받아서 수행해서 리턴해주는 간단한 구조다.
- 구현하는 클라이언트 측에서 예외를 캐치할 필요 없도록 throws 선언되어 있다.
- 주석의 설명에는 ResultSetExtractor는 주로 JDBC 프레임워크 내부에서 사용된다고 설명한다.
- RowMapper가 하나만 하던 여러 행을 하던 더 간단한 선택이라고 설명한다.
- 사실이 query메서드는 일반적으로 사용되는 query메서드는 아니다.

<br><br>

# JdbcTemplate#query

```java

@FunctionalInterface
public interface RowMapper<T> {
    @Nullable
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
```

```java
public<T> List<T> query(String sql,RowMapper<T> rowMapper)throws DataAccessException{
        return result(query(sql,new RowMapperResultSetExtractor<>(rowMapper)));
        }
// result 메서드는 Null 체크한다
```

```java
public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
    private final RowMapper<T> rowMapper;
    private final int rowsExpected;

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
        this(rowMapper, 0);
    }

    public RowMapperResultSetExtractor(RowMapper<T> rowMapper, int rowsExpected) {
        Assert.notNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
        this.rowsExpected = rowsExpected;
    }

    @Override
    public List<T> extractData(ResultSet rs) throws SQLException {
        List<T> results = (this.rowsExpected > 0 ? new ArrayList<>(this.rowsExpected) : new ArrayList<>());
        int rowNum = 0;
        while (rs.next()) {
            results.add(this.rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }
}
```

- 앞서 살펴본, ResultSetExtractor를 직접 다루는 코드는 클라이언트가 사용하기엔 불편한 점이 있다.
    - ResultSet을 다루는 모든 내용을 직접 작성해야 한다.
    - 따라서 클라이언트가 구현할 때 next() 선언을 해줘야 하고, 리스트로 매핑하려면 while문을 사용해 모든 내용을 다 구현해줘야 한다.
- 이러한 불편함을 극복하기 위해 나온 것이 RowMapper<T> 이다.
- 일반적으로 사용되는 메서드의 시그니처는 아래와 같다.
- `public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException`
- 사용자가 String sql,과 ResultSetExtractor<T> rse 를 전달하면, ResultSet을 다루는 모든 내용을 클라이언트가 직접 해야 한다
- 대신 String sql과 RowMapper<T>를 전달하면, 내부적으로 ResultSetExtractor를 구현한 RowMapperResultSetExtractor로 어댑팅해서 query 메서드를 호출해준다.
- RowMapper의 rowNum은 결과 반환을 위한 ArrayList의 생성자 매개변수로 전달된다. 리스트 초기화 시점에 크기를 결정한다. 만약 결과가 예상된다면 매개변수를 전달해서 최적화를 할 수 있는 건데..
  많은 행의 결과가 반복적으로 예상되는 쿼리가 있다면 전달해서 중간에 리스트의 크기를 반복해서 늘리는 작업을 제거할 수 있을 것 같다.
- rowMapper – the RowMapper which creates an object for each row
- rowsExpected – the number of expected rows (just used for optimized collection handling)
- 내부 구현에서 rowNum을 0에서 1씩 증가시키면서 mapRow에 전달해주고 있으므로, 구현체에서 rowNum을 0부터 시작해서 사용할 수도 있다.

<br><br>

# JdbcTemplate#queryForObject

```java
public<T> T queryForObject(String sql,RowMapper<T> rowMapper)throws DataAccessException{
        List<T> results=query(sql,rowMapper);
        return DataAccessUtils.nullableSingleResult(results);
        }
```

```java
public static<T> T nullableSingleResult(@Nullable Collection<T> results)throws IncorrectResultSizeDataAccessException{
        if(CollectionUtils.isEmpty(results)){
        throw new EmptyResultDataAccessException(1);
        }

        if(results.size()>1){
        throw new IncorrectResultSizeDataAccessException(1,results.size());
        }

        return results.iterator().next();
        }
```

- queryForObject는 1개의 결과를 기대할 때 사용하는 메서드이다.
- 앞서 살펴본 query 메서드와 시그니처가 메서드명 빼고 동일하다.
- 앞서 살펴본 query메서드를 실행한 후, 결과가 1개인지 검증해서 반환하는 것 뿐이다.

<br><br>

# JdbcTemplate#update

```java
public int update(final String sql)throws DataAccessException{

class UpdateStatementCallback implements StatementCallback<Integer>, SqlProvider {
    @Override
    public Integer doInStatement(Statement stmt) throws SQLException {
        int rows = stmt.executeUpdate(sql);
        if (logger.isTraceEnabled()) {
            logger.trace("SQL update affected " + rows + " rows");
        }
        return rows;
    }

    @Override
    public String getSql() {
        return sql;
    }
}

    return updateCount(execute(new UpdateStatementCallback(),true));
            }
```

- StatementCallback<T> 를 구현해서 execute만 호출하는 것을 알 수 있다.
- executeUpdate(sql)만 수행할 뿐이다. 심플하다.

<br><br>

# JdbcTemplate#batchUpdate(String… sql)

- stmt로부터 connection을 받아온 뒤, 배치 업데이트를 지원하는지 검증한다.
- 지원할 경우, 가변인자로 전달된 sql들을 세미콜론으로 묶어서 하나의 문장으로 append한다
    - 그리고 stmt의 addBatch 메서드를 통해 각각의 문장을 추가해준다. 이건 각 벤더 내부구현인듯.
    - 그리고 stmt의 executeBatch를 호출하면, int[] 타입으로 가변인자 sql들의 영향받은 행의 수를 반환받아 리턴한다.
- 지원하지 않을 경우, for문으로 돌면서 하나씩 수행하는 식이다… 오 이거;; 배치 업데이트를 벤더에서 지원하는지 확인하고 사용하는게 중요하겠구나…

<br><br><br>
