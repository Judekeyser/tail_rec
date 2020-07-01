package be.jdevelopment.tailrec.example;

import java.math.BigInteger;

public class FiboEssai implements FiboDirective {

    @Override
    public BigInteger fibonacci(int N) throws Exception {
        return new TailRecTrap().fibonacci(N);
    }

    @Override
    public Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        throw new UnsupportedOperationException();
    }

    private static final Object TOKEN = new Object();

    private static final class TailRecTrap implements FiboDirective {

        Object[] argsContainer = new Object[3];
        DummyEngine dummyEngine = new DummyEngine(argsContainer);

        @Override
        public BigInteger fibonacci(int N) throws Exception {
            return FiboDirective.super.fibonacci(N);
        }

        @Override
        public Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
            argsContainer[0] = prev;
            argsContainer[1] = current;
            argsContainer[2] = remainingIter;
            Object caught;
            while(true) {
                caught = dummyEngine.invoke();
                if (caught != TOKEN)
                    return caught;
            }
        }

    }

    private static final class DummyEngine implements FiboDirective {

        private final Object[] argsContainer;
        DummyEngine(Object[] argsContainer) {
            this.argsContainer = argsContainer;
        }

        Object invoke() {
            return FiboDirective.super._fibonacci(
                    (BigInteger) argsContainer[0],
                    (BigInteger) argsContainer[1],
                    (int) argsContainer[2]
            );
        }

        @Override
        public BigInteger fibonacci(int N) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
            argsContainer[0] = prev;
            argsContainer[1] = current;
            argsContainer[2] = remainingIter;
            return TOKEN;
        }

    }

}
