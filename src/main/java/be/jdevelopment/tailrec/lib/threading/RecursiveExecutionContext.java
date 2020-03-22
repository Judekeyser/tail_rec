package be.jdevelopment.tailrec.lib.threading;

public interface RecursiveExecutionContext {

    void assertLegitAccess() throws IllegalStateException;

    void relaxStorage();

    void setupContext();

    @FunctionalInterface interface MethodCall {
        Object call() throws Throwable;
    }
    Object awaitForResult(MethodCall methodCall) throws Throwable;

}
