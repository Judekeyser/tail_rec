package be.jdevelopment;

import be.jdevelopment.tailrec.example.Facto;
import be.jdevelopment.tailrec.example.Fibo;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class FactoTest {

    @State(Scope.Benchmark)
    public static class Config {
        public Facto facto = new Facto();
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void facto(Config config) throws Exception {
        config.facto.factorial(13);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void facto_native() throws Exception {
        primitive();
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @Test
    public void simple_factorial() throws Exception {
        System.out.println(new Config().facto.factorial(13));
    }

    @Test
    public void primitive() throws Exception {
        long start = 13;
        long remainingIterations = 13;
        while (--remainingIterations > 1)
            start *= remainingIterations;
    }

    /*
        Benchmark                Mode  Cnt          Score         Error  Units
        FactoTest.facto         thrpt    5    3094886,034 ±  276770,685  ops/s
        FactoTest.facto_native  thrpt    5  143248210,533 ± 2182619,171  ops/s
     */

}
