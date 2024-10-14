package com.techcourse.domain;

import java.util.Objects;

public class User {

    private final Long id;
    private final String account;
    private String password;
    private final String email;

    public User(final String account, final String password, final String email) {
        this.id = null;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public User(final long id, final String account, final String password, final String email) {
        this.id = id;
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

    public String getAccount() {
        return account;
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final User user)) {
            return false;
        }
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", account='" + account + '\'' +
               ", email='" + email + '\'' +
               ", password='" + password + '\'' +
               '}';
    }
}
