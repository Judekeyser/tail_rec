# Tail recursion in Java

This project illustrates an example of code generation via AOP
to bring tail recursion in Java.

## Explanations

Find a trial of pedagogical information
about code structure at
https://gist.github.com/Judekeyser/281aeb0fd9903da7245f0b2e3f7fc92e/

## Usage

### Using the ready-to-user `@TailRecursiveExecutor`

Assume you have a working tail recursive function like this
one:
```
public class Fibo {

    BigInteger fibonacci(int N) {
        if (N < 0) throw
                new IllegalArgumentException("Negative ranked Fibonacci is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
    }

    BigInteger _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        if (remainingIter == 0)
            return current;
        return _fibonacci(current, prev.add(current), remainingIter - 1);
    }

}
```
Turn it like this:
```
public class Fibo {

    public @TailRecursiveExecutor
    BigInteger fibonacci(int N) {
        if (N < 0) throw
                new IllegalArgumentException("Negative ranked Fibonacci is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return (BigInteger) _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
    }

    private @TailRecursive
    Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        if (remainingIter == 0)
            return current;
        return _fibonacci(current, prev.add(current), remainingIter - 1);
    }

}
```
Annotate both methods (privacy restriction is good practice only).

Make sure the the `@TailRecursive` method explicitly
returns an `Object`. If you don't, magic won't occur
(for your own safety).

That's all! Have fun.

### Using the customizable `@TailRecursiveEntry`

The `@TailRecursive` method requires to be executed in a
thread that also implement
`be.jdevelopment.tailrec.lib.threading.WithMethodExecutionContext`.
The previous `@TailRecursiveExecutor` method creates a new thread
with this feature, and let the recursive call occur inside of it.

If you prefer full control on execution, decorate the public method
with `@TailRecursiveEntry` instead of `@TailRecursiveExecution`.
It will first check if context is ok, then it will run the
`@TailRecursive` method in the current thread.

