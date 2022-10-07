package nextstep.jdbc;

import java.util.Objects;

public class TestUser {

    private Long id;
    private String email;
    private String password;

    public TestUser(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUser testUser = (TestUser) o;
        return Objects.equals(id, testUser.id) && Objects.equals(email, testUser.email) && Objects.equals(password, testUser.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password);
    }
}
