package callback;

import java.util.Optional;

public abstract class Task {

    public final void executeWith(Callback callback) {
        execute();
        Optional.ofNullable(callback)
                .ifPresent(Callback::call);
    }

    public abstract void execute();
}
