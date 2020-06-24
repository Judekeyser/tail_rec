package be.jdevelopment;

import be.jdevelopment.tailrec.example.Fibo;
import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;

public class FiboTest {

    @Test
    public void fibonacci() throws Exception {
        BigInteger largeNumber = new Fibo().fibonacci(1000000);

        String asStr = largeNumber.toString();

        assertTrue(asStr.startsWith("1953"));
        assertTrue(asStr.endsWith("875"));
        assertEquals(208988, asStr.length());

        System.out.println("1000th Fibonacci number is:");
        System.out.println(asStr);
    }

}
