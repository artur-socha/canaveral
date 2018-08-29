package pl.codewise.canaveral.core.runtime.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class LocalManagedDnsService {

    private static final Logger log = LoggerFactory.getLogger(LocalManagedDnsService.class);

    public static final String SERVICE_PROVIDER = "canaveral-it";
    public static final String SERVICE_TYPE = "dns";
    public static final String PROVIDER_NAME = SERVICE_TYPE + "," + SERVICE_PROVIDER;

    private static final String PROVIDER_DEFAULT = "default";
    private static final String PROVIDER_DNS_SUN = "dns,sun";

    private static final String PROPERTY_PROVIDER_NAME = "sun.net.spi.nameservice.provider.%d";
    private static final String PROPERTY_NETWORKADDRESS_CACHE_TTL = "networkaddress.cache.ttl";
    private static final String PROPERTY_NETWORKADDRESS_CACHE_NEGATIVE_TTL = "networkaddress.cache.negative.ttl";

    private static final String INET_ADDRESS_CLASS_NAME = "java.net.InetAddress";
    private static final String INET_ADDRESS_FIELD_NAME_SERVICES = "nameServices";
    private static final String INET_ADDRESS_METHOD_CREATE_NS_PROVIDER = "createNSProvider";
    private static final String CLASS_LOADER_METHOD_FIND_LOADED_CLASS = "findLoadedClass";

    private static final String JDK9_INET_ADDRESS_FIELD_NAME_SERVICE = "nameService";
    private static final String JDK9_INET_ADDRESS_NAME_SERVICE_CLASS_NAME = "java.net.InetAddress$NameService";

    public static void installService() {
        installService(false);
    }

    public static void installService(boolean useSunProvider) {
        log.debug("Setting up service");
        System.setProperty(PROPERTY_NETWORKADDRESS_CACHE_TTL, "0");
        System.setProperty(PROPERTY_NETWORKADDRESS_CACHE_NEGATIVE_TTL, "0");
        setupSystemProperties(useSunProvider);
        Class<?> inetAddressClass = getLoadedClass(INET_ADDRESS_CLASS_NAME);
        if (inetAddressClass != null) {
            setupInetAddress(useSunProvider, inetAddressClass);
        } else {
            try {
                ClassLoader.getSystemClassLoader().loadClass(INET_ADDRESS_CLASS_NAME);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void setupInetAddress(boolean useSunProvider, Class<?> inetAddressClass) {
        try {
            log.debug("Setting up inet address");
            Field nameServicesField = inetAddressClass.getDeclaredField(INET_ADDRESS_FIELD_NAME_SERVICES);
            nameServicesField.setAccessible(true);
            Method createNSProviderMethod = inetAddressClass.getDeclaredMethod
                    (INET_ADDRESS_METHOD_CREATE_NS_PROVIDER, new Class[] {String.class});
            createNSProviderMethod.setAccessible(true);
            List nameServices = (List) nameServicesField.get(null);
            nameServices.clear();
            nameServices.add(createNSProviderMethod.invoke(null, PROVIDER_NAME));
            if (useSunProvider) {
                nameServices.add(createNSProviderMethod.invoke(null, PROVIDER_DNS_SUN));
            }
            nameServices.add(createNSProviderMethod.invoke(null, PROVIDER_DEFAULT));
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            try {
                setupInetAddressForJdk9Plus(inetAddressClass);
            } catch (RuntimeException jdk9Exception) {
                jdk9Exception.addSuppressed(e);
                throw jdk9Exception;
            }
        }
    }

    private static void setupInetAddressForJdk9Plus(Class<?> inetAddressClass) {
        try {
            log.debug("Setting up inet address for jdk 9");
            Field nameServicesField = inetAddressClass.getDeclaredField(JDK9_INET_ADDRESS_FIELD_NAME_SERVICE);
            nameServicesField.setAccessible(true);
            NameServiceProxy fallback = new NameServiceProxy(nameServicesField.get(null));
            NameServiceProxy service = new NameServiceProxy(new LocalManagedDns(), fallback);

            Object typedNameService = Arrays.stream(inetAddressClass.getDeclaredClasses())
                    .filter(cl -> cl.getName().equals(JDK9_INET_ADDRESS_NAME_SERVICE_CLASS_NAME))
                    .findAny()
                    .map(service::exposeInterface)
                    .orElseThrow(() -> new RuntimeException("Not found: " + JDK9_INET_ADDRESS_NAME_SERVICE_CLASS_NAME));

            nameServicesField.set(null, typedNameService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setupSystemProperties(boolean useSunProvider) {
        int n = 1;
        System.setProperty(String.format(PROPERTY_PROVIDER_NAME, n++), PROVIDER_NAME);
        if (useSunProvider) {
            System.setProperty(String.format(PROPERTY_PROVIDER_NAME, n++), PROVIDER_DNS_SUN);
        }
        System.setProperty(String.format(PROPERTY_PROVIDER_NAME, n++), PROVIDER_DEFAULT);
    }

    private static Class<?> getLoadedClass(String className) {
        try {
            Method m = ClassLoader.class.getDeclaredMethod(CLASS_LOADER_METHOD_FIND_LOADED_CLASS, String.class);
            m.setAccessible(true);
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            return (Class<?>) m.invoke(cl, className);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}