package be.jdevelopment.tailrec;

import be.jdevelopment.tailrec.lib.strategy.TailRecursive;
import be.jdevelopment.tailrec.lib.threading.TailRecursiveExecutor;

import java.math.BigInteger;

class Fibo {

    @TailRecursiveExecutor
    BigInteger fibonacci(int N) {
        if (N < 0) throw
                new IllegalArgumentException("Negative ranked Fibonacci is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return (BigInteger) _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
    }

    @TailRecursive
    private Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        if (remainingIter == 0)
            return current;
        return _fibonacci(current, prev.add(current), remainingIter - 1);
    }

}
