package com.techcourse.domain;

public record User (Long id,String account,String password,String email) {

    public User(String account, String password, String email) {
        this(null, account, password, email);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public User changePassword(String newPassword) {
        return new User(id, account, newPassword, email);
    }
}
