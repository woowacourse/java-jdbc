package com.interface21.jdbc.support;

import java.util.Objects;

public class TestUser {

    private final Long id;
    private final String account;

    public TestUser(Long id, String account) {
        this.id = id;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestUser testUser = (TestUser) o;
        return Objects.equals(id, testUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, account);
    }
}
