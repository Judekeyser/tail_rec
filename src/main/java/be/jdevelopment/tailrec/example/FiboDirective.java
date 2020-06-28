package be.jdevelopment.tailrec.example;

import be.jdevelopment.tailrec.lib.annotations.TailRecursiveDirective;
import be.jdevelopment.tailrec.lib.annotations.TailRecursive;
import be.jdevelopment.tailrec.lib.annotations.TailRecursiveExecutor;

import java.math.BigInteger;

@TailRecursiveDirective(exportedAs = "Fibo")
interface FiboDirective {

    @TailRecursiveExecutor
    default BigInteger fibonacci(int N) throws Exception {
        if (N < 0) throw
                new IllegalArgumentException("Negative ranked Fibonacci is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return (BigInteger) _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
    }

    @TailRecursive
    default Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        if (remainingIter == 0)
            return current;
        return _fibonacci(current, prev.add(current), remainingIter - 1);
    }

}
