# jwp-dashboard-jdbc

## 개발 노트

- execute의 메소드롤 공통된 작업을 추상화하는 데 있어서, primitive 타입은 어떻게 generic 으로 처리할까? reference type을 감싸주는 메소드를
  사용해서 이를 해결한다. void를 리턴해주는 방식도 이와 같은 방식으로 Void를 리턴할 수 있으나, 지금의 구현에서는 변화된 row수를 반환해주도록 하자.

```java
public class JdbcTemplate {
    private static int updateCount(@Nullable Integer result) {
        Assert.state(result != null, "No update count");
        return result;
    }
}
```

## 구현 목표
- [x] Sql+args로 PreparedStatement를 만드는 로직을 분리하여, args가 있든 없든 공통 로직으로 묶을 수 있게끔 리팩토링
