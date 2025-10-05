package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(User.class.getSimpleName().toLowerCase()).append("s ");
        sb.append(" (");

        Field[] fields = user.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("id")) {
                continue;
            }

            sb.append(field.getName()).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());
        sb.append(") VALUES (");

        sb.append("?, ".repeat(Math.max(0, fields.length - 1)));
        sb.delete(sb.length() - 2, sb.length());
        sb.append(")");

        System.out.println(sb);

        jdbcTemplate.update(sb.toString(), mapParameters(user));
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(User.class.getSimpleName().toLowerCase()).append("s ");
        sb.append("SET");

        Field[] fields = user.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("id")) {
                continue;
            }

            sb.append(field.getName()).append(" = ?, ");
        }

        sb.deleteCharAt(sb.length() - 2);

        // TODO: 가변성 있게 바꾸기
        sb.append("WHERE id = ?");

        Object[] objects = mapParameters(user);
        Object[] newObjects = new Object[objects.length + 1];
        System.arraycopy(objects, 0, newObjects, 0, objects.length);
        newObjects[newObjects.length - 1] = user.getId();

        jdbcTemplate.update(sql, newObjects);
    }

    private Object[] mapParameters(Object obj) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            Object[] params = new Object[fields.length - 1];

            int paramIndex = 0;

            for (Field field : fields) {
                if (field.getName().equals("id")) {
                    continue;
                }
                field.setAccessible(true);
                params[paramIndex] = field.get(obj);
                paramIndex++;
            }

            return params;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.getResults((resultSet) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        ), sql, (Object[]) null);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.getSingleResult((resultSet) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        ), sql, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.getSingleResult((resultSet) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        ), sql, account);
    }
}
