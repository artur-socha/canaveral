package pl.codewise.canaveral.core.runtime.dns;

import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

public class LocalManagedDnsDescriptor implements NameServiceDescriptor {

    @Override
    public NameService createNameService() throws Exception {
        LocalManagedDns service = new LocalManagedDns();
        NameServiceProxy serviceProxy = new NameServiceProxy(service);
        return serviceProxy.exposeInterface(NameService.class);
    }

    @Override
    public String getProviderName() {
        return LocalManagedDnsService.SERVICE_PROVIDER;
    }

    @Override
    public String getType() {
        return LocalManagedDnsService.SERVICE_TYPE;
    }
}

