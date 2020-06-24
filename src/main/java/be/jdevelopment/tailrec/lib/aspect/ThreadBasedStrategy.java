package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;
import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;
import be.jdevelopment.tailrec.lib.threading.RecursiveContextBinder;
import be.jdevelopment.tailrec.lib.threading.WithMethodExecutionContext;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class ThreadBasedStrategy  extends RecursiveStrategyTemplate {
    static final ThreadBasedStrategy INSTANCE = new ThreadBasedStrategy();

    final static ConcurrentHashMap<String, RecursiveContextBinder> BINDER_REPOSITORY = new ConcurrentHashMap<>();
    private static final RecursiveContextBinder DEFAULT_CTX = new DefaultContextBinder();

    @Override protected ArgsContainer getArgsContainer() {
        return ((WithMethodExecutionContext) Thread.currentThread())
                .getMethodExecutionContext()
                .getArgsContainer();
    }

    RecursiveContextBinder getContextBinder(String tailRecursiveExecutorName) {
        return Optional.ofNullable(tailRecursiveExecutorName)
                .map(BINDER_REPOSITORY::get)
                .orElse(DEFAULT_CTX);
    }
}