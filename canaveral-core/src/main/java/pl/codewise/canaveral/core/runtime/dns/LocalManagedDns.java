package pl.codewise.canaveral.core.runtime.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalManagedDns {

    private static final ThreadLocal<Logger> logger = ThreadLocal.withInitial(
            () -> LoggerFactory.getLogger(LocalManagedDns.class)
    );

    public LocalManagedDns() {
    }

    public String getHostByAddr(byte[] ip) throws UnknownHostException {
        throw new UnknownHostException();
    }

    public InetAddress[] lookupAllHostAddr(String name) throws UnknownHostException {
        InetAddress ipAddress = NameStore.getInstance().get(name);
        if (ipAddress != null) {
            logger.get().debug("Resolved {} to {}", name, ipAddress);
            return new InetAddress[] {ipAddress};
        } else {
            throw new UnknownHostException(name);
        }
    }
}
