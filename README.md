# JDBC 라이브러리 구현하기 미션

# 1단계 

## 체크 사항
- UserDaoTest의 모든 테스트 케이스가 통과한다.
- UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

## 기능 요구사항
- 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이블러리르 직접 만들자

## 리펙터링 과정

### 기존의 코드를 템플릿 콜백 패턴으로 변경

- try-catch, 자원 반납을 Dao를 작성하는 클라이언트가 모르게끔 코드를 변경.

```java
public class UserDao extends DaoTemplateCallBack {

    public UserDao(final DataSource dataSource) {
        super(dataSource);
    }
    
    // ...
    
    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        super.execute(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        super.execute(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            pstmt.executeUpdate();
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return super.queryAll(sql, (pstmt, rs) -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                final var user = new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                users.add(user);
            }
            return users;
        });
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return super.queryOneLong(sql, id, (pstmt, rs) -> {
            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
            return null;
        });
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return super.queryOneString(sql, account, (pstmt, rs) -> {
            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
            return null;
        });
    }
}
```

```java
public abstract class DaoTemplateCallBack {

    private final DataSource dataSource;
    
    // ...

    protected void execute(final String sql, final ThrowingConsumer<PreparedStatement, SQLException> pstmtConsumer) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            pstmtConsumer.accept(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected <RET> RET queryOneLong(final String sql, final Long deliminator, final ThrowingBiFunction<PreparedStatement, ResultSet, RET, SQLException> pstmtFunction) {
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setLong(1, deliminator);
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            return pstmtFunction.apply(pstmt, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected <RET> RET queryOneString(final String sql, final String deliminator, final ThrowingBiFunction<PreparedStatement, ResultSet, RET, SQLException> pstmtFunction) {
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, deliminator);
            rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            return pstmtFunction.apply(pstmt, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected <RET> List<RET> queryAll(final String sql, final ThrowingBiFunction<PreparedStatement, ResultSet, List<RET>, SQLException> pstmtFunction) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
             final var rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            return pstmtFunction.apply(pstmt, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
```

