package com.techcourse.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserHistory {

    private Long id;

    private final long userId;
    private final String account;
    private final String password;
    private final String email;

    private final LocalDateTime createdAt;

    private final String createBy;

    public UserHistory(final User user, final String createBy) {
        this(null, user.getId(), user.getAccount(), user.getPassword(), user.getEmail(), createBy);
    }

    public UserHistory(final Long id, final long userId, final String account, final String password, final String email, final String createBy) {
        this.id = id;
        this.userId = userId;
        this.account = account;
        this.password = password;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.createBy = createBy;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreateBy() {
        return createBy;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        UserHistory that = (UserHistory) object;
        return userId == that.userId && Objects.equals(account, that.account) && Objects.equals(
                password, that.password) && Objects.equals(email, that.email) && Objects.equals(
                createBy, that.createBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, account, password, email, createBy);
    }
}
