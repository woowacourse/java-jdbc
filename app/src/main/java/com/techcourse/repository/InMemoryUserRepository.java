package com.techcourse.repository;

import com.techcourse.domain.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

    private static final InMemoryUserRepository inMemoryUserRepository;

    private static final Map<String, User> database = new ConcurrentHashMap<>();

    static {
        inMemoryUserRepository = new InMemoryUserRepository();
        final User user = new User(1, "gugu", "password", "hkkang@woowahan.com");
        database.put(user.getAccount(), user);
    }

    public static InMemoryUserRepository getInstance() {
        return inMemoryUserRepository;
    }

    @Override
    public void save(User user) {
        database.put(user.getAccount(), user);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return Optional.ofNullable(database.get(account));
    }

    private InMemoryUserRepository() {
    }
}
