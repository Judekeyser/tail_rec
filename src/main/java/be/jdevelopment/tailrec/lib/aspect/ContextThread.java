package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;
import be.jdevelopment.tailrec.lib.threading.MethodExecutionContext;
import be.jdevelopment.tailrec.lib.threading.WithMethodExecutionContext;

class ContextThread extends Thread implements WithMethodExecutionContext {

    private final MethodExecutionContext ctx = new MethodExecutionContextBasicImpl();

    ContextThread(Runnable r) {
        super(r);
    }

    @Override public MethodExecutionContext getMethodExecutionContext() {
        return ctx;
    }

    private static class MethodExecutionContextBasicImpl implements MethodExecutionContext {

        private ArgsContainer proxy;

        @Override public ArgsContainer getArgsContainer() {
            return proxy;
        }

        @Override public void setArgsContainer(ArgsContainer argsContainer) {
            this.proxy = argsContainer;
        }

    }
}