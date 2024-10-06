package com.techcourse.fixture;

import com.techcourse.domain.User;

public enum UserFixture {

    GUGU("gugu", "password", "hkkang@woowahan.com"),
    DORA("dora", "password", "dora@woowahan.com");

    private final String account;
    private final String password;
    private final String email;

    UserFixture(String account, String password, String email) {
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public User user() {
        return new User(account, password, email);
    }

    public String account() {
        return account;
    }

    public String password() {
        return password;
    }

    public String email() {
        return email;
    }
}
