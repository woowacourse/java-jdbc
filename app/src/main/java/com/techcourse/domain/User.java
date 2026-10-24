package com.techcourse.domain;

public class User {

    private static final int MAX_LENGTH = 100;

    private Long id;
    private final String account;
    private String password;
    private final String email;

    public User(long id, String account, String password, String email) {
        this(account, password, email);
        this.id = id;
    }

    public User(String account, String password, String email) {
        this.account = validate("account", account);
        this.password = validate("password", password);
        this.email = validate("email", email);
    }

    private static String validate(final String name, final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(name + " must be at most " + MAX_LENGTH + " characters");
        }
        return value;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void changePassword(String password) {
        this.password = validate("password", password);
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
