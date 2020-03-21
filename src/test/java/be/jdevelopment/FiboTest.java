package be.jdevelopment;

import be.jdevelopment.tailrec.Fibo;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class FiboTest {

    @Test
    public void fibonacci() {
        BigInteger largeNumber = new Fibo().fibonacci(1000000);

        String asStr = largeNumber.toString();

        assertTrue(asStr.startsWith("1953"));
        assertTrue(asStr.endsWith("875"));
        assertEquals(208988, asStr.length());
    }

}
