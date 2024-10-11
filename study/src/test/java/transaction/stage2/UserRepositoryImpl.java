package transaction.stage2;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("delete from users");
    }

    @Override
    public void save(User user) {
        jdbcTemplate.update(
                "insert into users(account, password, email) values(?,?,?)",
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("select * from users", (rs, rowNum) -> new User(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)));
    }
}
