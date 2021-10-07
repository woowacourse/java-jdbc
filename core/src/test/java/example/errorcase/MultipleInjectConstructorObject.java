package example.errorcase;

import di.annotation.Inject;

public class MultipleInjectConstructorObject {

    private String field;

    @Inject
    public MultipleInjectConstructorObject() {

    }

    @Inject
    public MultipleInjectConstructorObject(String field) {
        this.field = field;
    }
}
