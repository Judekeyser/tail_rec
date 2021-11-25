import be.jdevelopment.tailrec.TailRecursive;

import java.math.BigInteger;

public class TailRecursiveTest {

  interface Fibo {
    default BigInteger compute (int rank) {
      final BigInteger ONE = BigInteger.valueOf(1L);
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
                 (p,m,a) -> java.lang.reflect.InvocationHandler .invokeDefault(p,m,a) // Don't ask too much questions - security concern
               );

    for (var rank : new int[] { 0, 1, 2, 3, 4, 5, 10, 50_000 })
      System.out.printf("The Fibonacci number of rank %d is %d\n", rank, fibo .compute (rank));
  }
}


