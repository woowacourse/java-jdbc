package callback;

import java.util.Optional;

public abstract class Task {

    final void executeWith(Callback callback) {
        execute();
        Optional.ofNullable(callback)
                .ifPresent(Callback::call);
    }

    final void executeAdd(AddMethod addMethod, int a, int b) {
        execute();
        Optional.ofNullable(addMethod)
                .ifPresent(it -> it.add(a, b));
    }

    public abstract void execute();
}
