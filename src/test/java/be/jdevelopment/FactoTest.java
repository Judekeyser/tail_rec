package be.jdevelopment;

import be.jdevelopment.tailrec.example.Facto;
import be.jdevelopment.tailrec.example.FactoDirective;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import main.Main;

public class FactoTest {

    static int N = 13;

    @State(Scope.Benchmark)
    public static class Config {
        public Facto facto = new Facto();
        public FactoDirective primFacto = new FactoDirective() {};
        public Main scala = new Main();
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void streamFacto13(Config config) throws Exception {
        IntStream.rangeClosed(1, N)
                .mapToObj(Stack::initialize)
                .reduce((a,b) -> a.combine(b));
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void recFacto13(Config config) throws Exception {
        config.primFacto.factorial(N);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void tailrecFacto13(Config config) throws Exception {
        config.facto.factorial(N);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void scalaFacto13(Config config) throws Exception {
        config.scala.facto(N);
    }

    private static final class Stack {
        BigInteger stack;
        int remainingIteration;

        static Stack initialize(int N) {
            var stack = new Stack();
            stack.remainingIteration = N - 1;
            stack.stack = BigInteger.valueOf(1L);
            return stack;
        }

        Stack combine(Stack current) {
            var stack = new Stack();
            stack.remainingIteration = current.remainingIteration - 1;
            stack.stack = current.stack.multiply(BigInteger.valueOf(current.remainingIteration));
            return stack;
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    /*
Comparison with streams, rec, tail rec and scala tail rec

N = 13

Benchmark                 Mode  Cnt    Score    Error  Units
FactoTest.recFacto13      avgt    5  265,270 ±  0,282  ns/op
FactoTest.scalaFacto13    avgt    5  304,201 ±  2,605  ns/op
FactoTest.streamFacto13   avgt    5  481,922 ± 52,967  ns/op
FactoTest.tailrecFacto13  avgt    5  368,343 ±  0,759  ns/op
     */

}
