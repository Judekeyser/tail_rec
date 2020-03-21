package be.jdevelopment.tailrec.lib.threading;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;

public interface MethodExecutionContext {

    ArgsContainer getArgsContainer();

    void setArgsContainer(ArgsContainer argsContainer);

}
