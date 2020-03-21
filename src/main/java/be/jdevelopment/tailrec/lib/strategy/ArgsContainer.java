package be.jdevelopment.tailrec.lib.strategy;

public interface ArgsContainer {

    static ArgsContainer getInstance() {
        return new ArgsContainerImpl();
    }

    void setArgs(Object[] args);
    Object[] getArgs();

}
