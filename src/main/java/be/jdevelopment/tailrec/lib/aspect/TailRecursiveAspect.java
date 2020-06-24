package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategy;
import be.jdevelopment.tailrec.lib.threading.RecursiveContextBinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
public class TailRecursiveAspect<T> {

    /* Dynamic invokers */

    private final Object monitor = new Object();
    private MethodHandle aroundTailRec;
    private final String tailRecConfig;
    public TailRecursiveAspect(String tailRecConfig) {
        this.tailRecConfig = tailRecConfig;
    }

    public void initializeAroundTailRec(Class<?> directive, String methodName, Class<?> namespace) {
        if (aroundTailRec != null) return;
        synchronized (monitor) {
            if (aroundTailRec != null) return;
            try {
                MethodType jvmType = MethodType.methodType(Object.class, Object[].class, namespace);
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                aroundTailRec = lookup.findVirtual(directive, methodName, jvmType);
            } catch(Throwable e) {
                throw new Error(e);
            }
        }
    }

    /* Utils from directive implementations */

    @FunctionalInterface
    public interface MethodCallProvider {
        RecursiveStrategy.MethodCall provide (MethodHandle handle);
    }

    public Object aroundTailRecAdvice(MethodCallProvider methodCallProvider, RecursiveStrategy.ArgsProvider provider) {
        Objects.requireNonNull(aroundTailRec);
        RecursiveStrategy.MethodCall call = methodCallProvider.provide(aroundTailRec);
        return weakenAroundTailRecAdvice(call, provider);
    }

    private <T extends Throwable> Object weakenAroundTailRecAdvice(
            RecursiveStrategy.MethodCall methodCall,
            RecursiveStrategy.ArgsProvider provider) throws T {
        try {
            return ThreadBasedStrategy.INSTANCE.tailRecTrap(methodCall, provider);
        } catch(Throwable e) {
            throw (T) e;
        }
    }

    public Object aroundExecutorAdvice(RecursiveContextBinder.MethodCall methodCall) {
        Objects.requireNonNull(tailRecConfig);
        return weakenAroundExecutorAdvice(methodCall);
    }

    private <T extends Throwable> Object weakenAroundExecutorAdvice(RecursiveContextBinder.MethodCall methodCall) throws T {
        try {
            return ThreadBasedStrategy.INSTANCE.getContextBinder(tailRecConfig)
                    .bindInContext(methodCall);
        } catch(Throwable e) {
            throw (T) e;
        }
    }

    /* Implementation specific */

    public static void register(String binderKey, RecursiveContextBinder contextBinder) {
        ThreadBasedStrategy.BINDER_REPOSITORY.put(binderKey, contextBinder);
    }

}
