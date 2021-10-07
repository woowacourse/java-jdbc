package examples;

import annotation.Inject;

public class Parent {

    @Inject
    private final String injectField = "injectField";

    private final String notInjectField = "notInjectField";

}
