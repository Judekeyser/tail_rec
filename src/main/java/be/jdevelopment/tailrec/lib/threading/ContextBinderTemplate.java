package be.jdevelopment.tailrec.lib.threading;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class ContextBinderTemplate implements RecursiveContextBinder {

    @FunctionalInterface
    protected interface ContextualizedRunner {
        void run(MethodExecutionContext ctx);
    }
    abstract protected void executeInContext(ContextualizedRunner runnable);

    @Override public final Object executeInContext(MethodCall methodCall) throws Throwable {
        ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
        ArrayBlockingQueue<Optional<Throwable>> executionFailed = new ArrayBlockingQueue<>(1);
        executeInContext(ctx -> {
            var execFailedValue = Optional.<Throwable> empty();
            ctx.setArgsContainer(ArgsContainer.getInstance());
            ctx.getArgsContainer().setArgs(null);
            try {
                queue.offer(methodCall.call());
            } catch(Throwable error) {
                execFailedValue = Optional.of(error);
            } finally {
                ctx.getArgsContainer().setArgs(null);
                ctx.setArgsContainer(null);
                executionFailed.offer(execFailedValue);
            }
        });

        var maybeException = executionFailed.take();
        if (maybeException.isPresent()) {
            throw maybeException.get();
        }
        return queue.take();
    }

}
