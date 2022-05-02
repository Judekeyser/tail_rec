import be.jdevelopment.tailrec.TailRecursive;

import java.lang.reflect.InvocationHandler;
import java.math.BigInteger;

import static java.math.BigInteger.valueOf;

public class TailRecursiveTest {

  interface Fibo {
    default BigInteger compute (int rank) {
      var ONE = valueOf(1L);
      return (BigInteger) _compute (rank, ONE, ONE);
    }

    default Object _compute (int rank, BigInteger s1, BigInteger s0) {
      return switch (rank) {
        case 0  -> s0;
        case 1  -> s1;
        default -> _compute (rank - 1, s0.add(s1), s1);
      };
    }
  }

  public static void main(String... args) {
    var fibo = TailRecursive .optimize (
            Fibo.class, // The class you want to optimize
            "_compute", // The name of the tail-recursive method
            InvocationHandler::invokeDefault // Don't ask too much questions - security concern
    );

    for (var rank : new int[] { 0, 1, 2, 3, 4, 5, 10, 100_000 })
      System.out.printf("The Fibonacci number of rank %d is %d\n", rank, fibo .compute (rank));
  }
}


