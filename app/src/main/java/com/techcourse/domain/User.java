package com.techcourse.domain;

public class User {

    private static final int MAX_FIELD_LENGTH = 100;

    private Long id;
    private final String account;
    private String password;
    private final String email;

    public User(long id, String account, String password, String email) {
        this(account, password, email);
        this.id = id;
    }

    public User(String account, String password, String email) {
        validateField(account, "계정");
        validateField(password, "비밀번호");
        validateField(email, "이메일");
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void changePassword(String password) {
        validateField(password, "비밀번호");
        this.password = password;
    }

    private static void validateField(final String value, final String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + "은(는) 필수 입력값입니다.");
        }
        if (value.length() > MAX_FIELD_LENGTH) {
            throw new IllegalArgumentException(name + "은(는) " + MAX_FIELD_LENGTH + "자 이하여야 합니다.");
        }
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
