package example.errorcase;

import example.notcomponent.User;

public class DuplicateParametersObject {

    private final User user;
    private final User admin;

    public DuplicateParametersObject(User user, User admin) {
        this.user = user;
        this.admin = admin;
    }
}
