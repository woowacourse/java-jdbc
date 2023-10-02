package org.springframework.jdbc.core;

import java.util.Objects;

class TestUser {
    public static final RowMapper<TestUser> TEST_USER_ROW_MAPPER = resultSet -> new TestUser(resultSet.getString(1));

    private final String name;

    public TestUser(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestUser user = (TestUser) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
