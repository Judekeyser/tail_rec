package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;
import be.jdevelopment.tailrec.lib.threading.ContextStorageTemplate;
import be.jdevelopment.tailrec.lib.threading.WithMethodExecutionContext;

import java.util.function.Supplier;

class Context extends ContextStorageTemplate implements Supplier<ArgsContainer> {

    @Override public ArgsContainer get() {
        return ((WithMethodExecutionContext) Thread.currentThread())
                .getMethodExecutionContext()
                .getArgsContainer();
    }

}
