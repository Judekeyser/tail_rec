package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;
import be.jdevelopment.tailrec.lib.threading.ContextBinderTemplate;
import be.jdevelopment.tailrec.lib.threading.MethodExecutionContext;
import be.jdevelopment.tailrec.lib.threading.WithMethodExecutionContext;

class MethodExecutionContextBasicImpl extends ContextBinderTemplate
        implements MethodExecutionContext, WithMethodExecutionContext {

    @Override
    public MethodExecutionContext getMethodExecutionContext() {
        return this;
    }

    private ArgsContainer proxy;

    @Override
    public ArgsContainer getArgsContainer() {
        return proxy;
    }

    @Override
    public void setArgsContainer(ArgsContainer argsContainer) {
        this.proxy = argsContainer;
    }

    @Override protected void executeInContext(ContextualizedRunner runnable) {
        runnable.run(this);
    }

}
