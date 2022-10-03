package nextstep.jdbc.support;

import java.util.Objects;
import nextstep.jdbc.RowMapper;

public class User {

    public static RowMapper<User> ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private Long id;
    private final String account;
    private final String password;
    private final String email;

    public User(final Long id, final String account, final String password, final String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
