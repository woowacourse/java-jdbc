package com.techcourse.domain;

import lombok.Getter;

@Getter
public class User {

    private final Long id;
    private final String account;
    private final String email;
    private String password;

    public User(final long id, final String account, final String password, final String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public User(final String account, final String password, final String email) {
        this.id = null;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public boolean checkPassword(final String password) {
        return this.password.equals(password);
    }

    public void changePassword(final String password) {
        this.password = password;
    }
}
