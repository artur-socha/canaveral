package pl.codewise.canaveral.mock.eureka;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.codewise.canaveral.core.runtime.ProgressAssertion;
import pl.codewise.canaveral.core.runtime.RunnerContext;

import java.util.concurrent.TimeUnit;

public class EurekaHasAppRegistered implements ProgressAssertion {

    private static final Logger log = LoggerFactory.getLogger(EurekaHasAppRegistered.class);
    private final String appName;
    private final Duration maxWaitFor;
    private final InstanceInfo.InstanceStatus status;

    public EurekaHasAppRegistered(String appName) {
        this(appName, java.time.Duration.ofMinutes(1));
    }

    public EurekaHasAppRegistered(String appName, java.time.Duration maxWaitTime) {
        this(appName, maxWaitTime, InstanceInfo.InstanceStatus.UP);
    }

    public EurekaHasAppRegistered(String appName, java.time.Duration maxWaitTime, InstanceInfo.InstanceStatus status) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appName), "App name cannot be empty.");
        Preconditions.checkNotNull(status, "Status cannot be null!");
        this.appName = appName;
        this.maxWaitFor = new Duration(maxWaitTime.toMillis(), TimeUnit.MILLISECONDS);
        this.status = status;
    }

    @Override
    public boolean canProceed(RunnerContext cache) {
        EurekaMockProvider eurekaMockProvider = cache.getMock(EurekaMockProvider.class);
        Awaitility.await()
                .atMost(maxWaitFor)
                .pollInterval(Duration.ONE_HUNDRED_MILLISECONDS)
                .until(() -> eurekaMockProvider.getAllApplications().stream()
                        .filter(app -> appName.equalsIgnoreCase(app.getName()))
                        .flatMap(app -> app.getInstances().stream())
                        .peek(instanceInfo -> log.trace("Checking app {} with status {}.", instanceInfo.getAppName(),
                                instanceInfo.getStatus()))
                        .map(InstanceInfo::getStatus)
                        .filter(status::equals)
                        .findFirst()
                        .orElse(null) != null);
        log.info("Found {} APP which is UP!", appName);
        return true;
    }
}
