package com.techcourse.domain;

import java.time.LocalDateTime;

public class UserHistory {

    private Long id;

    private final long userId;
    private final String account;
    private final String password;
    private final String email;

    private final LocalDateTime createdAt;

    private final String createdBy;

    public UserHistory(final User user, final String createdBy) {
        this(null, user.getId(), user.getAccount(), user.getPassword(), user.getEmail(), createdBy);
    }

    public UserHistory(final Long id, final long userId, final String account, final String password, final String email, final String createdBy) {
        this.id = id;
        this.userId = userId;
        this.account = account;
        this.password = password;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    public UserHistory(Long id, long userId, String account, String password, String email, LocalDateTime createdAt,
                       String createdBy) {
        this.id = id;
        this.userId = userId;
        this.account = account;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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

    public String getCreatedBy() {
        return createdBy;
    }
}
