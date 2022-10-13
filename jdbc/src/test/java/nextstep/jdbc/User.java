package nextstep.jdbc;

import java.util.Objects;

public class User {

    private final String account;
    private final String password;
    private final String email;

    public User(final String account, final String password, final String email) {
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
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
        return Objects.equals(getAccount(), user.getAccount()) && Objects.equals(getPassword(),
                user.getPassword()) && Objects.equals(getEmail(), user.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), getPassword(), getEmail());
    }
}
