package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;
import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;

import java.util.function.Supplier;

class Strategy extends RecursiveStrategyTemplate {

    private final Supplier<ArgsContainer> argsContainerSupplier;

    Strategy(Supplier<ArgsContainer> argsContainerSupplier) {
        this.argsContainerSupplier = argsContainerSupplier;
    }

    @Override protected ArgsContainer getArgsContainer() {
        return argsContainerSupplier.get();
    }
}
