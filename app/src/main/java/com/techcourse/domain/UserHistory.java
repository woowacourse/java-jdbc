package com.techcourse.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserHistory {

    private final Long id;
    private final long userId;
    private final String account;
    private final String password;
    private final String email;
    private final LocalDateTime createdAt;
    private final String createBy;

    public UserHistory(final User user, final String createBy) {
        this(null, user.getId(), user.getAccount(), user.getPassword(), user.getEmail(), createBy);
    }

    public UserHistory(final Long id, final long userId, final String account, final String password,
                       final String email, final String createBy) {
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final UserHistory userHistory)) {
            return false;
        }
        return Objects.equals(getId(), userHistory.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserHistory{" +
               "id=" + id +
               ", userId=" + userId +
               ", account='" + account + '\'' +
               ", password='" + password + '\'' +
               ", email='" + email + '\'' +
               ", createdAt=" + createdAt +
               ", createBy='" + createBy + '\'' +
               '}';
    }
}
