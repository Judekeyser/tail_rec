package be.jdevelopment.tailrec.lib.threading;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class ContextBinderTemplate implements RecursiveContextBinder {

    protected abstract void executeInContext(Runnable runnable);

    @Override public final Object bindInContext(MethodCall methodCall) throws Throwable {
        ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
        executeInContext(() -> {
            getCurrentContext().setArgsContainer(ArgsContainer.getInstance());
            getCurrentContext().getArgsContainer().setArgs(null);
            try {
                queue.offer(methodCall.call());
            } catch(Throwable error) {
                queue.offer(error);
                throw new RuntimeException(error);
            } finally {
                getCurrentContext().getArgsContainer().setArgs(null);
                getCurrentContext().setArgsContainer(null);
            }
        });
        return queue.take();
    }

    private MethodExecutionContext getCurrentContext() {
        return ((WithMethodExecutionContext) Thread.currentThread()).getMethodExecutionContext();
    }

}
