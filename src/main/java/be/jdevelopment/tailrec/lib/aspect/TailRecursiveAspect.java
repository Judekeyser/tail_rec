package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategy;
import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;
import be.jdevelopment.tailrec.lib.threading.RecursiveContextBinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
public class TailRecursiveAspect<T> extends RecursiveStrategyTemplate {

    /* Dynamic invokers */

    private final Object monitor = new Object();
    private MethodHandle aroundTailRec;
    private final MethodExecutionContextBasicImpl contextHolder;
    public TailRecursiveAspect() {
        super(new MethodExecutionContextBasicImpl());
        contextHolder = (MethodExecutionContextBasicImpl) ctxProvider;
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

    @SuppressWarnings("unchecked")
    private <T extends Throwable> Object weakenAroundTailRecAdvice(
            RecursiveStrategy.MethodCall methodCall,
            RecursiveStrategy.ArgsProvider provider
    ) throws T {
        try {
            return tailRecTrap(methodCall, provider);
        } catch(Throwable e) {
            throw (T) e;
        }
    }

    public Object aroundExecutorAdvice(RecursiveContextBinder.MethodCall methodCall) {
        return weakenAroundExecutorAdvice(methodCall);
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> Object weakenAroundExecutorAdvice(RecursiveContextBinder.MethodCall methodCall) throws T {
        try {
            return contextHolder.executeInContext(methodCall);
        }
        catch (Throwable e) {
            throw (T) e;
        }
    }

}
