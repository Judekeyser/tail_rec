package be.jdevelopment.tailrec.lib.strategy;

public abstract class RecursiveStrategyTemplate implements RecursiveStrategy {

    protected abstract ArgsContainer getArgsContainer();

    @Override public final Object tailRecTrap(MethodCall methodCall, ArgsProvider argsProvider) throws Throwable {
        ArgsContainer argsContainer = getArgsContainer();

        if (argsContainer.getArgs() != null) {
            argsContainer.setArgs(argsProvider.getArgs());
            return breakChainStrategy();
        }

        try {
            argsContainer.setArgs(argsProvider.getArgs());
            return trapStrategy(methodCall, argsContainer);
        } finally {
            argsContainer.setArgs(null);
        }
    }

    private final static Object PROOF = new Object();

    private Object trapStrategy(MethodCall methodCall, ArgsContainer argsContainer) throws Throwable {
        Object caught;
        while (true) {
            caught = methodCall.call(argsContainer.getArgs());
            if (caught != PROOF)
                return caught;
        }
    }

    private Object breakChainStrategy() {
        return PROOF;
    }

}
