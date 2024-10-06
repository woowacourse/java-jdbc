package com.interface21.jdbc.support;

public class TestUser {

    private final Long id;
    private final String account;

    public TestUser(Long id, String account) {
        this.id = id;
        this.account = account;
    }

    private TestUser() {
        this(null, null);
    }

    public Long getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }
}
