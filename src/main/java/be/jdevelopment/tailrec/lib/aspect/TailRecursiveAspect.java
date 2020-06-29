package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategy;
import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;
import be.jdevelopment.tailrec.lib.threading.RecursiveContextBinder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
public class TailRecursiveAspect<T> extends RecursiveStrategyTemplate {

    /* Dynamic invokers */

    private final Object monitor = new Object();
    private MethodHandle aroundTailRec;
    private final MethodExecutionContextBasicImpl contextHolder;
    public TailRecursiveAspect() {
        super(new MethodExecutionContextBasicImpl());
        contextHolder = (MethodExecutionContextBasicImpl) ctxProvider;
    }

    @FunctionalInterface
    public interface MethodCallProvider {
        RecursiveStrategy.MethodCall provide (MethodHandle handle);
    }
    public <U extends Enum<U>> void initializeAroundTailRec(Class<?> directive, String methodName, Class<U> namespace, MethodCallProvider templateMethodCall) {
        if (aroundTailRec != null) return;
        synchronized (monitor) {
            if (aroundTailRec != null) return;
            try {
                MethodType jvmType = MethodType.methodType(Object.class, Object[].class, namespace);
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                aroundTailRec = lookup.findVirtual(directive, methodName, jvmType);
                this.methodCall = templateMethodCall.provide(aroundTailRec);
            } catch(Throwable e) {
                throw new Error(e);
            }
        }
    }

    /* Utils from directive implementations */

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

    /* Improvement test */

    private Object[] caughtArgs = null;
    public void registerArgs(Object[] args) {
        this.caughtArgs = args;
    }

    RecursiveStrategy.ArgsProvider provider = () -> caughtArgs;
    RecursiveStrategy.MethodCall methodCall;

    @SuppressWarnings("unchecked")
    public <T extends Throwable> Object aroundTailRecAdvice() throws T {
        try {
            return tailRecTrap(methodCall, provider);
        } catch(Throwable e) {
            throw (T) e;
        }
    }

}
