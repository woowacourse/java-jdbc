package com.techcourse.domain;

public class User {

    private Long id;
    private final String account;
    private String password;
    private final String email;

    public User(final long id, final String account, final String password, final String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public User(final String account, final String password, final String email) {
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

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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
