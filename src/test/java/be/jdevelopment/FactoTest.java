package be.jdevelopment;

import be.jdevelopment.tailrec.example.Facto;
import be.jdevelopment.tailrec.example.FactoDirective;
import be.jdevelopment.tailrec.example.Fibo;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.IntStream;

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
    public void streamFacto13(Config config) throws Exception {
        IntStream.rangeClosed(1, 13)
                .mapToObj(Stack::initialize)
                .reduce((a,b) -> a.combine(b));
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void tailrecFacto13(Config config) throws Exception {
        config.facto.factorial(13);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void recFacto13(Config config) throws Exception {
        config.primFacto.factorial(13);
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
Benchmark              Mode  Cnt    Score    Error  Units
FactoTest.facto1       avgt    5  120,893 ±  1,142  ns/op
FactoTest.facto12      avgt    5  555,909 ± 10,833  ns/op
FactoTest.facto13      avgt    5  604,642 ±  5,380  ns/op
FactoTest.facto2       avgt    5  138,250 ±  1,705  ns/op
FactoTest.primFacto12  avgt    5  231,809 ±  1,776  ns/op
FactoTest.primFacto13  avgt    5  273,726 ±  1,456  ns/op
FactoTest.primFacto2   avgt    5    0,761 ±  0,016  ns/op

Comparison with streams, rec and tail rec

Benchmark                 Mode  Cnt    Score    Error  Units
FactoTest.recFacto13      avgt    5  295,467 ± 37,038  ns/op
FactoTest.streamFacto13   avgt    5  484,473 ± 76,233  ns/op
FactoTest.tailrecFacto13  avgt    5  482,303 ± 25,753  ns/op
     */

}
