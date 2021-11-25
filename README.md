# Forewords
MIT License

Copyright (c) 2019 Judekeyser

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# Tail recursion utility

This utility is concerned with bringing a bit of tail recursion in Java.

## Example

Assume you have a tail recursive definition of the Fibonacci numbers:
```java
interface Fibo {
  default BigInteger compute (int rank) {
    final BigInteger ONE = BigInteger.valueOf(1L);
    return (BigInteger) _compute (rank, ONE, ONE);
  }

  default Object _compute (int rank, BigInteger s1, BigInteger s0) {  // (1)
    return switch (rank) {
      case 0  -> s0;
      case 1  -> s1;
      default -> _compute (rank - 1, s0.add(s1), s1);
    };
  }
}
```
We provide a Proxy utility to instantiate that algorithm in a tail recursive fashion:
```java
var fibo = TailRecursive .optimize (
             Fibo.class, // (2)
             "_compute", // (3)
             (p,m,a) -> java.lang.reflect.InvocationHandler .invokeDefault(p,m,a) // (4)
           );

for (var rank : new int[] { 0, 1, 2, 3, 4, 5, 10, 50_000 })
  System.out.printf("The Fibonacci number of rank %d is %d\n", rank, fibo .compute (rank));
```

## Library limitations

The following limitations currently exist on the library, without a clear hope to reduce them.
(All contributions are welcome!)

### Return type must be `Object`
The return type of the tail recursive method must be `Object`. In general this is not a big deal because:
1. as the method is tail recursive, the return type is not expected to be used in the recursive method
2. usually the tail recursive call can be wrapped inside a safe, initial arguments manager method, that can take care of the cast.

### The class to optimized must be passed
The class to optimized must be passed as a parameter. It should be an interface.
In exchange, the typing of the return type is inferred as an instance of the interface.

### The tail recursive method name must be passed
The tail recursive method name must be passed as an argument.
It is currently not possible to annotate this method and refer to it by reflexion, as it may not be reachable (different module).

### The `InvocationHandler` must be passed
An instance of `InvocationHandler` must be passed. This invocation handler will be in charge of dispatching a method on the created intance.
In our use case, it's a method-default dispatch strategy.

This limitation exists for security concerns too (different module).

## Performance
Our approach is less performant than a regular recursive invocation, as it uses under the hood a total of three handlers to fake recursion.

## Build

The build is minimalist on purpose. The library in itself is only one file.






