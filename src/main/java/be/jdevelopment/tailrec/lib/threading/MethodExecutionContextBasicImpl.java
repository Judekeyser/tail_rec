package be.jdevelopment.tailrec.lib.threading;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;

class MethodExecutionContextBasicImpl implements MethodExecutionContext {

    private ArgsContainer proxy;

    @Override public ArgsContainer getArgsContainer() {
        return proxy;
    }

    @Override public void setArgsContainer(ArgsContainer argsContainer) {
        this.proxy = argsContainer;
    }

}
