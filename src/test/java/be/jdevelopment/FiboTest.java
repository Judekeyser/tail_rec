package be.jdevelopment;

import be.jdevelopment.tailrec.example.Fibo;
import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;

public class FiboTest {

    Fibo fibo = new Fibo();

    @Test
    public void fibonacci() throws Exception {
        BigInteger largeNumber = fibo.fibonacci(1000000);

        String asStr = largeNumber.toString();

        assertTrue(asStr.startsWith("1953"));
        assertTrue(asStr.endsWith("875"));
        assertEquals(208988, asStr.length());

        System.out.println("1000th Fibonacci number is:");
        System.out.println(asStr);
    }

    @Test
    public void fibonacci_again() throws Exception {
        assertEquals(5, fibo.fibonacci(5).intValue());
    }

    @Test
    public void fibonacci_throwsAsExpected() {
        try {
            fibo.fibonacci(-1);
            fail("Should have thrown");
        }
        catch (IllegalArgumentException ignored) { }
        catch (Exception e) {
            fail("Should have been an instance of IllegalArgumentException");
        }
    }

}
