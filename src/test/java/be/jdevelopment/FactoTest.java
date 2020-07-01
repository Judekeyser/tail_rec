package be.jdevelopment;

import be.jdevelopment.tailrec.example.Facto;
import be.jdevelopment.tailrec.example.FactoDirective;
import be.jdevelopment.tailrec.example.Fibo;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

import main.Main;

public class FactoTest {

    @State(Scope.Benchmark)
    public static class Config {
        public Facto facto = new Facto();
        public FactoDirective primFacto = new FactoDirective() {};
        public Main scala = new Main();
    }

    @Fork(value = 1, warmups = 1)
    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void streamFacto13(Config config) throws Exception {
        IntStream.rangeClosed(1, 13)
                .mapToObj(Stack::initialize)
                .reduce((a,b) -> a.combine(b));
    }

    @Fork(value = 1, warmups = 1)
    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void recFacto13(Config config) throws Exception {
        config.primFacto.factorial(13);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void tailrecFacto13(Config config) throws Exception {
        config.facto.factorial(13);
    }

    @Fork(value = 1, warmups = 1)
    //@Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void scalaFacto13(Config config) throws Exception {
        config.scala.facto(13);
    }

    private static final class Stack {
        Long stack;
        int remainingIteration;

        static Stack initialize(int N) {
            var stack = new Stack();
            stack.remainingIteration = N - 1;
            stack.stack = 1L;
            return stack;
        }

        Stack combine(Stack current) {
            var stack = new Stack();
            stack.remainingIteration = current.remainingIteration - 1;
            stack.stack = current.stack * current.remainingIteration;
            return stack;
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    /*
Comparison with streams, rec and tail rec

Benchmark                 Mode  Cnt    Score   Error  Units
FactoTest.recFacto13      avgt    5   36,452 ± 1,465  ns/op
FactoTest.scalaFacto13    avgt   10  298,172 ± 1,423  ns/op
FactoTest.streamFacto13   avgt    5  222,342 ± 2,395  ns/op
FactoTest.tailrecFacto13  avgt   10  270,908 ± 5,224  ns/op
     */

}
