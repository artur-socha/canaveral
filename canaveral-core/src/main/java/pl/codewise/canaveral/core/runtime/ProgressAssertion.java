package pl.codewise.canaveral.core.runtime;

@FunctionalInterface
public interface ProgressAssertion {

    ProgressAssertion CAN_PROGRESS_ASSERTION = (context) -> true;

    boolean canProceed(RunnerContext runnerContext);
}
