package pl.codewise.canaveral.core.runtime;

@SuppressWarnings("unused")
public interface LifeCycleListener {

    default int getPriority() {
        return 1;
    }

    default void afterAllMocksCreated(RunnerContext runnerContext) {

    }
}
