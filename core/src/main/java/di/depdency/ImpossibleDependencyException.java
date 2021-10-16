package di.depdency;

import exception.CoreException;

public class ImpossibleDependencyException extends CoreException {

    public ImpossibleDependencyException(String message) {
        super(message);
    }
}
