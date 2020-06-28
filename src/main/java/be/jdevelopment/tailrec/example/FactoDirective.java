package be.jdevelopment.tailrec.example;

import be.jdevelopment.tailrec.lib.annotations.TailRecursive;
import be.jdevelopment.tailrec.lib.annotations.TailRecursiveDirective;
import be.jdevelopment.tailrec.lib.annotations.TailRecursiveExecutor;

import java.math.BigInteger;

@TailRecursiveDirective(exportedAs = "Facto")
interface FactoDirective {

    @TailRecursiveExecutor
    default long factorial(int N) throws Exception {
        if (N < 0) throw
                new IllegalArgumentException("Factorial of negative numbers is not defined");
        if (N == 0 || N == 1)
            return 1L;
        return (long) _factorial(N, N - 1);
    }

    @TailRecursive
    default Object _factorial(long current, int remainingIter) {
        if (remainingIter == 1)
            return current;
        return _factorial(current * remainingIter, remainingIter - 1);
    }

}
