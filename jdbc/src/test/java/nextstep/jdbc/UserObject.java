package nextstep.jdbc;

import java.util.Objects;

public class UserObject {

    private final Long id;
    private final String account;
    private final String password;
    private final String email;

    public UserObject(Long id, String account, String password, String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public Long getId() {
        return id;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserObject that = (UserObject) o;
        return Objects.equals(id, that.id) && Objects.equals(account, that.account)
                && Objects.equals(password, that.password) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, account, password, email);
    }
}
