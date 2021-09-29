package com.techcourse.repository;

import com.techcourse.domain.User;

import com.techcourse.exception.DuplicateAccountException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryUserRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryUserRepository.class);

    private final Map<String, User> database;
    private final AtomicLong autoIncrementId;

    public InMemoryUserRepository() {
        this(new ConcurrentHashMap<>(), new AtomicLong(1));
    }

    public InMemoryUserRepository(Map<String, User> database, AtomicLong autoIncrementId) {
        this.database = database;
        this.autoIncrementId = autoIncrementId;
    }

    public void save(User user) {
        if (database.containsKey(user.getAccount())) {
            LOGGER.debug("Duplicate account already exist => {}", user.getAccount());
            throw new DuplicateAccountException(String.format("%s 와 동일한 계정이 존재합니다.", user.getAccount()));
        }

        User newUser = User.generateId(autoIncrementId.getAndIncrement(), user);
        database.put(newUser.getAccount(), newUser);
    }

    public Optional<User> findByAccount(String account) {
        return Optional.ofNullable(database.get(account));
    }
}
