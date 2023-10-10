package nextstep.jdbc;

import java.util.Objects;

public class TestUser {
    private final String email;

    private final String password;

    public TestUser(final String email, final String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestUser)) {
            return false;
        }
        final TestUser testUser = (TestUser) o;
        return Objects.equals(email, testUser.email) && Objects.equals(password, testUser.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }
}
