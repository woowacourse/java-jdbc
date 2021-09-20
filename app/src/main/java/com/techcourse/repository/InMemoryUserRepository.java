package com.techcourse.repository;

import com.techcourse.domain.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import nextstep.mvc.exception.BadRequestException;
import nextstep.web.annotation.Repository;

@Repository
public class InMemoryUserRepository {

    private static final int DEFAULT_USER_ID = 0;
    private static final AtomicLong id = new AtomicLong(0L);

    private final Map<String, User> database = new ConcurrentHashMap<>();

    public InMemoryUserRepository() {
        final User user = new User(id.incrementAndGet(), "gugu", "password", "hkkang@woowahan.com");
        final User user2 = new User(id.incrementAndGet(), "mungto", "password", "mungto@gmail.com");
        database.put(user.getAccount(), user);
        database.put(user2.getAccount(), user2);
    }

    public void save(User user) {
        if (database.containsKey(user.getAccount())) {
            throw new BadRequestException();
        }
        userIdValidate(user);
        database.put(user.getAccount(), user);
    }

    private void userIdValidate(User user) {
        if (user.equalsId(DEFAULT_USER_ID)) {
            user.setId(id.incrementAndGet());
        }
    }

    public Optional<User> findByAccount(String account) {
        return Optional.ofNullable(database.get(account));
    }
}
