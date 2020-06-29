package be.jdevelopment.tailrec.lib.strategy;

public interface RecursiveStrategy {

    @FunctionalInterface interface MethodCall {
        Object call(Object[] args) throws Throwable;
    }

    Object tailRecTrap(MethodCall methodCall, Object[] args) throws Throwable;

}
