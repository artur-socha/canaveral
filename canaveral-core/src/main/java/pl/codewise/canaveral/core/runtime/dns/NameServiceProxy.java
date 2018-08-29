package pl.codewise.canaveral.core.runtime.dns;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.UnknownHostException;

public class NameServiceProxy implements InvocationHandler {

    private final Object target;
    private final Method lookupAllHostAddr;
    private final Method getHostByAddr;
    private final NameServiceProxy fallback;

    public NameServiceProxy(Object target) throws NoSuchMethodException {
        this(target, null);
    }

    public NameServiceProxy(Object target, NameServiceProxy fallback) throws NoSuchMethodException {
        this.target = target;
        this.getHostByAddr = target.getClass().getDeclaredMethod("getHostByAddr", byte[].class);
        this.getHostByAddr.setAccessible(true);
        this.lookupAllHostAddr = target.getClass().getDeclaredMethod("lookupAllHostAddr", String.class);
        this.lookupAllHostAddr.setAccessible(true);
        this.fallback = fallback;
    }

    public <T> T exposeInterface(Class<T> ifc) {
        return (T) Proxy.newProxyInstance(ifc.getClassLoader(), new Class[] {ifc}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invokeWithFallback(method.getName(), args);
    }

    private Object invokeWithFallback(String methodName, Object[] args) throws Throwable {
        try {
            return findMethod(methodName)
                    .invoke(target, args[0]);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof UnknownHostException && fallback != null) {
                return fallback.invokeWithFallback(methodName, args);
            }
            throw e.getCause();
        }
    }

    private Method findMethod(String methodName) {
        if ("lookupAllHostAddr".equals(methodName)) {
            return lookupAllHostAddr;
        } else if ("getHostByAddr".equals(methodName)) {
            return getHostByAddr;
        } else {
            throw new UnsupportedOperationException("Method not supported: " + methodName);
        }
    }
}
