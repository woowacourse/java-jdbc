package example.errorcase;

import di.annotation.Component;
import example.notcomponent.Name;
import example.notcomponent.User;

public class DuplicationParametersMethod {

    @Component
    public User user(Name firstName, Name lastName) {
        return new User();
    }

}
