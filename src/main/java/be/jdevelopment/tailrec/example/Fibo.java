package be.jdevelopment.tailrec.example;

import be.jdevelopment.tailrec.lib.aspect.TailRecursiveAspect;

public final class Fibo extends FiboDirective {
    TailRecursiveAspect<Fibo> aspect = new TailRecursiveAspect<>("default-executor");

    private enum NAMESPACE {SELF;}

    public Object _fibonacci(Object[] args, NAMESPACE self) {
        assert self != null;
        return super._fibonacci(
                (java.math.BigInteger) args[0],
                (java.math.BigInteger) args[1],
                (int) args[2]
        );
    }

    protected Object _fibonacci(java.math.BigInteger prev, java.math.BigInteger current, int remainingIter) {
        return aspect.aroundTailRecAdvice(
                $ -> args -> $.invokeExact(this, args, NAMESPACE.SELF),
                () -> new Object[] {prev, current, remainingIter}
                );
    }

    public java.math.BigInteger fibonacci(int N) throws Exception {
        aspect.initializeAroundTailRec(Fibo.class, "_fibonacci", NAMESPACE.class);
        return (java.math.BigInteger) aspect.aroundExecutorAdvice(() -> super.fibonacci(N));
    }
}