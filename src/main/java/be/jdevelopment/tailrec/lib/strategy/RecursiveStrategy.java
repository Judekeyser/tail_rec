package be.jdevelopment.tailrec.lib.strategy;

public interface RecursiveStrategy {

    @FunctionalInterface interface ArgsProvider {
        Object[] getArgs();
    }
    @FunctionalInterface interface MethodCall {
        Object call(Object[] args) throws Throwable;
    }

    Object tailRecTrap(MethodCall methodCall, ArgsProvider argsProvider) throws Throwable;

}
