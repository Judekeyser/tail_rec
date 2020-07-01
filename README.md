# Tail recursion in Java

This project illustrates an example of code generation via annotation processing
to bring tail recursion in Java.

Note: A previous version of the project was available using Aspect Oriented Programming.
After migrating to JDK14, we decided to slightly review the aspect coding and we finally
decided to abandon AOP in favor of annotation processing. This is hopefully closer to
basic standard tool proposed by JVM.

## Explanations

Find a trial of pedagogical information
about code structure at
https://gist.github.com/Judekeyser/281aeb0fd9903da7245f0b2e3f7fc92e/

Note: At this point, the note is written using the previous *v0/AOP-oriented* code.
From the algorithmic point of view, only minor changes were brought to the mechanism we propose,
hence the note remains a good starting point for anyone eager to learn how things work in more details.

## Usage

### Using the annotations

Define a tail recursive algorithm using a interface **contract**:
```java
interface Fibo {

    default BigInteger fibonacci(int N) throws Exception {
        if (N < 0) throw
                new IllegalArgumentException("Negative ranked Fibonacci is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
    }

    default BigInteger _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        if (remainingIter == 0)
            return current;
        return _fibonacci(current, prev.add(current), remainingIter - 1);
    }

}
```
Bring the following changes to the contract:
1. annotate `fibonacci` with `@TailRecursiveExecutor`
2. annotate `_fibonacci` with `@TailRecursive`
3. annotate the whole contract with `@TailRecursiveDirective` and refactor its name to add `Directive`
5. change the return type of `_fibonacci` to `Object` (and make the cast in `fibonacci`)
```java
@TailRecursiveDirective(exportedAs = "Fibo")
interface FiboDirective {

    @TailRecursiveExecutor
    default BigInteger fibonacci(int N) throws Exception {
        if (N < 0) throw
                new IllegalArgumentException("Negative ranked Fibonacci is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return (BigInteger) _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
    }

    @TailRecursive
    default Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        if (remainingIter == 0)
            return current;
        return _fibonacci(current, prev.add(current), remainingIter - 1);
    }

}
```
Letting the `be.jdevelopment.tailrec.lib.processor.TailRecDirectiveProcessor`
run on your project will create a subclass of `FiboDirective`
whose name is `Fibo`.

The generated class is `public final` and implements the `FiboDirective` contract.
However, there is one subtle thing: the `_fibonacci` has been overwritten in such a way that it now triggers an
`UnsupportedOperationException`. This is to prevent external user from using the method without coming from
the executor method `fibonacci`.

This is important from two perspectives:
1. you usually do not want a user to directly use `_fibonacci` and bypass input argument validation.
2. from a technical point of view, the `fibonacci` method will aso perform inner state reset and initialization,
hence its importance.

Why not letting `FiboDirective` be a class then?
Because **we believe** that a tail recursive algorithm is more like a method contract and better fit the
definition of interface. Indeed, as far as the end user is concerned, so object is mirroring the essence of
tail recursive algorithm: **the notion of internal state is void and thus, it does not really enter the 
definition of an Object**, from OOP point of view.

## Benchmark comparison

In order to evaluate the quality of the deployed strategy,
we have realized micro benchmarks between four different
approaches (see the `FactoTest` that computed factorial numbers):

1. The usual recursive way, in Java
2. The tail recursive way using the generated class (our approach)
3. The stream foldLeft way, using foldLeft like operation on a stream
4. The Scala built-in tail recursive way, using idiomatic Scala code.

The following results are from runnig `13!`:
```text
Benchmark                 Mode  Cnt    Score    Error  Units
FactoTest.recFacto13      avgt    5  265,270 ±  0,282  ns/op
FactoTest.scalaFacto13    avgt    5  304,201 ±  2,605  ns/op
FactoTest.tailrecFacto13  avgt    5  368,343 ±  0,759  ns/op  << us
FactoTest.streamFacto13   avgt    5  481,922 ± 52,967  ns/op
```