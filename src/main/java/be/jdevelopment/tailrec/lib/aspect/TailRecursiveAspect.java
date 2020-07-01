package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategy;
import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;
import be.jdevelopment.tailrec.lib.threading.RecursiveContextBinder;

public class TailRecursiveAspect<T> extends RecursiveStrategyTemplate {

    /* Dynamic invokers */

    private final MethodExecutionContextBasicImpl contextHolder;
    public TailRecursiveAspect(MethodCall methodCall) {
        super(new MethodExecutionContextBasicImpl());
        contextHolder = (MethodExecutionContextBasicImpl) ctxProvider;
        this.methodCall = methodCall;
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

    private final RecursiveStrategy.MethodCall methodCall;

    @SuppressWarnings("unchecked")
    public <T extends Throwable> Object aroundTailRecAdvice() throws T {
        try {
            return tailRecTrap(methodCall, caughtArgs);
        } catch(Throwable e) {
            throw (T) e;
        }
    }

}
