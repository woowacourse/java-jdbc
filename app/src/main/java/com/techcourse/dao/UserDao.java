package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UserDao {

    private final DataSource dataSource;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = """
                insert into users 
                (account, password, email)
                values (?, ?, ?)
                """;

        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            log.debug("query : {}", sql);
        }
    }

    public void update(final User user) {
        final var sql = """
                update users 
                set account = ?,
                    password = ?,
                    email = ?
                where id = ?
                """;

        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            log.debug("query : {}", sql);
        }
    }

    public List<User> findAll() {
        final var sql = """
                select id, account, password, email 
                from users
                """;

        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            final ResultSet rs = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            final List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }
            return users;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            log.debug("query : {}", sql);
        }
    }

    public Optional<User> findById(final Long id) {
        final var sql = """
                select id, account, password, email 
                from users
                where id = ?
                """;

        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            final ResultSet rs = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.of(
                        new User(
                                rs.getLong(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getString(4)));
            }
            return Optional.empty();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            log.debug("query : {}", sql);
        }
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = """
                select id, account, password, email 
                from users
                where account = ?
                """;

        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, account);
            final ResultSet rs = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.of(
                        new User(
                                rs.getLong(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getString(4)));
            }
            return Optional.empty();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            log.debug("query : {}", sql);
        }
    }
}
