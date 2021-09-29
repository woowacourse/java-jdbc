package nextstep.jdbc;

import java.util.Objects;

public class TestUser {
    private String account;
    private String email;
    private Long id;
    private String password;

    public TestUser(long id, String account, String password, String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUser testUser = (TestUser) o;
        return Objects.equals(account, testUser.account) && Objects.equals(email, testUser.email) && Objects.equals(id, testUser.id) && Objects.equals(password, testUser.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, email, id, password);
    }

    @Override
    public String toString() {
        return "TestUser{" +
                "account='" + account + '\'' +
                ", email='" + email + '\'' +
                ", id=" + id +
                ", password='" + password + '\'' +
                '}';
    }
}
