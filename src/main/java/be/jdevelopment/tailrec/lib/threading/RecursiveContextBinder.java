package be.jdevelopment.tailrec.lib.threading;

public interface RecursiveContextBinder {

    @FunctionalInterface interface MethodCall {
        Object call() throws Throwable;
    }
    Object executeInContext(MethodCall methodCall) throws Throwable;

}
