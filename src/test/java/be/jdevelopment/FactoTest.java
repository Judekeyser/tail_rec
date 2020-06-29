package be.jdevelopment;

import be.jdevelopment.tailrec.example.Facto;
import be.jdevelopment.tailrec.example.FactoDirective;
import be.jdevelopment.tailrec.example.Fibo;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FactoTest {

    @State(Scope.Benchmark)
    public static class Config {
        public Facto facto = new Facto();
        public FactoDirective primFacto = new FactoDirective() {};
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void primFacto12(Config config) throws Exception {
        config.primFacto.factorial(12);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void primFacto13(Config config) throws Exception {
        config.primFacto.factorial(13);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void facto13(Config config) throws Exception {
        config.facto.factorial(13);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void facto12(Config config) throws Exception {
        config.facto.factorial(12);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void facto1(Config config) throws Exception {
        config.facto.factorial(1);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void facto2(Config config) throws Exception {
        config.facto.factorial(2);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    /*
        Benchmark                Mode  Cnt          Score         Error  Units
        FactoTest.facto         thrpt    5    3094886,034 ±  276770,685  ops/s
        FactoTest.facto_native  thrpt    5  143248210,533 ± 2182619,171  ops/s
     */

}
