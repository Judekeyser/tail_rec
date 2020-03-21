package be.jdevelopment.tailrec.lib.strategy;

class ArgsContainerImpl implements ArgsContainer {

    ArgsContainerImpl() {}

    private Object[] arguments;

    @Override public void setArgs(Object[] args) {
        this.arguments = args;
    }

    @Override public Object[] getArgs() {
        return this.arguments;
    }

}
