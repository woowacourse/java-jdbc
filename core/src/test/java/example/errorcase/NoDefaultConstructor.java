package example.errorcase;

import di.annotation.Inject;
import example.notcomponent.User;

public class NoDefaultConstructor {

    @Inject
    private User user;

    public NoDefaultConstructor(User user) {
        this.user = user;
    }
}
