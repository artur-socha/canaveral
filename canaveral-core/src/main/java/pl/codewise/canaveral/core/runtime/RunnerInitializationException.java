package pl.codewise.canaveral.core.runtime;

public class RunnerInitializationException extends IllegalStateException {

    public RunnerInitializationException(Throwable cause) {
        super("Could not initialize runner.", cause);
    }
}
