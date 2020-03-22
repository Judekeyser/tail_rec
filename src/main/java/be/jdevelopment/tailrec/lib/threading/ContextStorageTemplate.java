package be.jdevelopment.tailrec.lib.threading;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class ContextStorageTemplate implements RecursiveExecutionContext {

    @Override public final void assertLegitAccess() {
        if (!(Thread.currentThread() instanceof WithMethodExecutionContext))
        throw new IllegalStateException("Unable to launch TailRecursive method without an execution context provider");
    }

    @Override public final void relaxStorage() {
        try {
            getCurrentContext().setArgsContainer(null);
        } catch(RuntimeException ignored) {}
    }

    @Override public final void setupContext() {
        getCurrentContext().setArgsContainer(ArgsContainer.getInstance());
    }

    @Override public final Object awaitForResult(MethodCall methodCall) throws Throwable {
        ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
        new ContextThread(() -> {
            setupContext();
            try {
                queue.offer(methodCall.call());
            } catch(Throwable error) {
                queue.offer(error);
                throw new RuntimeException(error);
            } finally {
                relaxStorage();
            }
        }).start();
        return queue.take();
    }

    private MethodExecutionContext getCurrentContext() {
        return ((WithMethodExecutionContext) Thread.currentThread()).getMethodExecutionContext();
    }

}
