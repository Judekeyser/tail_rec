package be.jdevelopment.tailrec.example;

import be.jdevelopment.tailrec.lib.annotations.TailRecursive;
import be.jdevelopment.tailrec.lib.annotations.TailRecursiveDirective;
import be.jdevelopment.tailrec.lib.annotations.TailRecursiveExecutor;

import java.math.BigInteger;

@TailRecursiveDirective(exportedAs = "Facto")
public interface FactoDirective {

    @TailRecursiveExecutor
    default BigInteger factorial(int N) throws Exception {
        if (N < 0) throw
                new IllegalArgumentException("Factorial of negative numbers is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return (BigInteger) _factorial(BigInteger.valueOf(N), N - 1);
    }

    @TailRecursive
    default Object _factorial(BigInteger current, int remainingIter) {
        if (remainingIter == 1)
            return current;
        return _factorial(current.multiply(BigInteger.valueOf(remainingIter)), remainingIter - 1);
    }

}
