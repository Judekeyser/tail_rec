package be.jdevelopment.tailrec.lib.threading;

public interface RecursiveContextBinder {

    @FunctionalInterface interface MethodCall {
        Object call() throws Throwable;
    }
    Object bindInContext(MethodCall methodCall) throws Throwable;

}
