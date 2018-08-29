package pl.codewise.canaveral.core.runtime.dns;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.util.IPAddressUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class NameStore {

    private static final Logger log = LoggerFactory.getLogger(NameStore.class);

    public static final InetAddress ADDR_LOOPBACK = InetAddress.getLoopbackAddress();

    private static AtomicReference<NameStore> instance = new AtomicReference<>();

    private Map<String, InetAddress> routingTable = new ConcurrentHashMap<>();

    public NameStore loopback(String hostName) {
        return this.route(hostName, ADDR_LOOPBACK);
    }

    public NameStore route(String hostName, String ipAddress) {
        try {
            Preconditions.checkArgument(ipAddress != null);
            Preconditions.checkArgument(IPAddressUtil.isIPv4LiteralAddress(ipAddress) || IPAddressUtil
                    .isIPv6LiteralAddress(ipAddress));
            return this.route(hostName, InetAddress.getByName(ipAddress));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Cannot convert '" + ipAddress + "' to InetAddress", e);
        }
    }

    public NameStore route(String hostName, InetAddress inetAddress) {
        hostName = StringUtils.trimToNull(hostName);
        Preconditions.checkArgument(hostName != null);
        Preconditions.checkArgument(inetAddress != null);
        routingTable.put(hostName, inetAddress);
        return this;
    }

    public NameStore defaultRoute(String hostName) {
        hostName = StringUtils.trimToNull(hostName);
        Preconditions.checkArgument(hostName != null);
        routingTable.remove(hostName);
        return this;
    }

    InetAddress get(String hostName) {
        log.debug("Looking up hostname = {} in routingTable = {}", hostName, routingTable.entrySet());
        return routingTable.get(hostName);
    }

    public static NameStore getInstance() {
        if (instance.get() == null) {
            NameStore nameStore = new NameStore();
            instance.compareAndSet(null, nameStore);
        }
        return instance.get();
    }

    public NameStore install() {
        LocalManagedDnsService.installService(false);
        return this;
    }

    public NameStore installWithSunDnsProvider() {
        LocalManagedDnsService.installService(true);
        return this;
    }
}