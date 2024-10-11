package transaction.stage2;

import java.util.List;

public interface UserRepository{
    void deleteAll();

    void save(User user);

    List<User> findAll();
}
