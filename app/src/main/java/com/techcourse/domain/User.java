package com.techcourse.domain;

import java.util.Objects;

public class User {

    private final Long id;
    private final String account;
    private final Password password;
    private final String email;

    public User(Long id, String account, String password, String email) {
        this.id = id;
        this.account = account;
        this.password = new Password(password);
        this.email = email;
    }

    public User(Long id, User user) {
        this(id, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public User(String account, String password, String email) {
        this(null, account, password, email);
    }

    public boolean checkPassword(String password) {
        Password target = new Password(password);
        return target.equals(this.password);
    }

    public User changePassword(String password) {
        return new User(this.id, this.account, password, this.getEmail());
    }

    public String getAccount() {
        return account;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
