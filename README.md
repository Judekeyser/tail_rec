# Tail recursion in Java

This project illustrates an example of code generation via AOP
to bring tail recursion in Java.

## Introduction

### Tail recursive methods

A method is *tail recursive* if its recursive call always occurs
as a tail call. For example, the method
```
int f (int start) {
    if (start == 0) return 0;
    return 1 + f(start - 1);
}
```
is not tail recursive, since the last instruction shows a
recursive call (a call to `f`) and this call is not a tail call:
after computing `y := f(start - 1)`, we still should evaluate 
`1 + y` before exiting.

A contrario, the method
```
int f (int start, int stack) {
    if (start == 0) return stack;
    return f(start - 1, stack + 1);
}
```
is tail recursive: when calling recursively `f` from a stack,
every other operation has been performed.

From the algorithmic point of view, both implementations
are valid implementations of the mathematic function
```latex
f(N) = \sum^N_{i=0} i
```

### Tail recursive pattern

Let us focus on a well-known example: the
Fibonacci numbers. A tail recursive, clean
implementation in Java may be done with
two methods:
```
@TailRecursiveExecutor
static BigInteger fibonacci(int N) {
    if (N < 0) throw
            new IllegalArgumentException("Negative ranked Fibonacci is not defined");
    if (N == 0 || N == 1)
        return BigInteger.valueOf(1L);
    BigInteger[] stack = {BigInteger.valueOf(1L), BigInteger.valueOf(1L)};
    return _fibonacci(stack, N - 2);
}

@TailRecursive
static BigInteger _fibonacci(BigInteger[] stack, int remainingIter) {
    if (remainingIter == 0)
        return stack[1];
    BigInteger buff = stack[0];
    stack[0] = stack[1];
    stack[1] = buff.add(stack[0]);
    return _fibonacci(stack, remainingIter - 1);
}
```
The tail recursive function is `_fibonacci`. Its access is private, as we do not
trust the user about the array: it is non null? does it have length 2? Nor about the
remaining number of iterations: is it non negative? Rather, we expose a public executor
`fibonnaci`, and we take the responsibility of calling `_fibonacci`. This is a common
design and a wise good practice...

At this point, the above annotations are defined only for code
readability, both with methods as target element types and
retention on class policy:
```
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface TailRecursiveExecutor { }

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface TailRecursive { }
```

### Project promise

Calling `fibonnaci` on a large number yields an error:
```
fibonacci(1000000); // java.lang.StackOverflowError
```
The aim of this project is to make the above code work
**without stack overflow** in a reasonable time, with **as few modifications**
as possible.
``` 
fibonacci(1000000); // 1953...875 in ~16s wiht ~constant memory
```
We advertise the reader that we have the bad habit to benchmark/monitor performance using
`currentTimeMillis()` and Windows embedded monitor tool; in other words: no benchmark ;-)

Any contribution on this topic is welcome!

### Code modification

Tail recursivity is almost all about code readability. The end user should thus
bring as few modification as possible. The only modification we propose is the following:
change
```
@TailRecursive BigInteger _fibonacci(BigInteger[] stack, int remainingIter)
```
to
```
@TailRecursive Object _fibonacci(BigInteger[] stack, int remainingIter) {
```
and in consequence, change also
```
return _fibonacci(stack, N - 2);
```
to
```
return (BigInteger) _fibonacci(stack, N - 2);
```
This should not afraid the reader. Indeed, the method `_fibonacci` *is expected to be*
tail recursive, hence no operation should be perform on it inside its definition.
Thanks to Java boxing/unboxing, primitives may be automatically assimilated to objects
(and if you're really concern with heap size, maybe you should wrap your primitives inside a mutable
object (an array, or a custom container)).

In addition, as `_fibonacci` is welcome to stay private, the public `fibonacci` takes
the responsibility of casting, which is no real big deal as we know, as programmers, that
the final result of `_fibonacci` is a `BigInteger` instance.

## Going into the code

### Tail recursive strategy

The major part of the job we'll have to do to avoid a stack overflow is
to break the recursivity. We need to be able to distinguish between the two following situations:
1. The result of `_fibonacci` is a *end result* (`BigInteger`, `Integer`, `Long`, whatever)
2. The result of `_fibonacci` is a call to itself.

After all, in the second situation, we do not really need to perform another operation:
we can just steal the next arguments of the call, and restart the computation with these new arguments!

The tail recursive trap goes as follow:
```
public @FunctionalInterface interface ArgsProvider {
    Object[] getArgs();
}
public @FunctionalInterface interface MethodCall {
    Object call(Object[] args) throws Throwable;
}
public final Object tailRecTrap(MethodCall methodCall, ArgsProvider argsProvider) throws Throwable {
    ArgsContainer argsContainer = getArgsContainer(); // To be defined

    if (argsContainer.getArgs() != null) {
        argsContainer.setArgs(argsProvider.getArgs());
        return breakChainStrategy();
    }

    try {
        argsContainer.setArgs(argsProvider.getArgs());
        return trapStrategy(methodCall, argsContainer);
    } finally {
        argsContainer.setArgs(null);
    }
}
```
According to whether the argument container `argsContainer` already has arguments to
provide, we take the decision between two strategies. The `trapStartegy` really
goes like a trap for the native recursive flow:
```
private final RecursiveCallProof PROOF = () -> null;
private Object trapStrategy(MethodCall methodCall, ArgsContainer argsContainer) throws Throwable {
    Object caught;
    while (true) {
        caught = methodCall.call(argsContainer.getArgs());
        if (caught != PROOF)
            return caught;
    }
}
```
The `PROOF` instance is an interface of some custom interface of ours, namely:
`RecursiveCallProof`. The idea is that it works as a flag: the end-user recursive method
**will not** return `PROOF`, hence it really help us to break the flow. The interface in itself
is not of interest: we only need a private final instance of it.

The `breakChainStrategy` goes as follow:
```
private Object breakChainStrategy() {
    return PROOF;
}
```
As a whole, the tail recursive strategy is relevantly grouped in an abstract class:
```
public abstract class RecursiveStrategyTemplate {

    protected abstract ArgsContainer getArgsContainer();

    public @FunctionalInterface interface ArgsProvider {
        Object[] getArgs();
    }
    public @FunctionalInterface interface MethodCall {
        Object call(Object[] args) throws Throwable;
    }
    public final Object tailRecTrap(MethodCall methodCall, ArgsProvider argsProvider) throws Throwable {
        ArgsContainer argsContainer = getArgsContainer();

        if (argsContainer.getArgs() != null) {
            argsContainer.setArgs(argsProvider.getArgs());
            return breakChainStrategy();
        }

        try {
            argsContainer.setArgs(argsProvider.getArgs());
            return trapStrategy(methodCall, argsContainer);
        } finally {
            argsContainer.setArgs(null);
        }
    }

    private final static RecursiveCallProof PROOF = () -> null;
    private Object trapStrategy(MethodCall methodCall, ArgsContainer argsContainer) throws Throwable {
        Object caught;
        while (true) {
            caught = methodCall.call(argsContainer.getArgs());
            if (caught != PROOF)
                return caught;
        }
    }
    private Object breakChainStrategy() {
        return PROOF;
    }

}
```

### Argument container

The `ArgsContainer` interface required to define a concrete version of the `RecursiveStrategyTemplate`
is as elementary as this:
```
public interface ArgsContainer {

    static ArgsContainer getInstance() {
        return new ArgsContainerImpl();
    }

    void setArgs(Object[] args);
    Object[] getArgs();

}
```
There is not much to about the structure of this interface.
Nevertheless, we still should be prepared that nothing in `_fibonacci` was designed
to provide an arguments-container: the signature of the function is
```
Object _fibonacci(int[] stack, int remainingIter)
```
and none of the two arguments may tweaked as we did for the `PROOF`, because they
are strongly typed and **we do not want to change that**. Remember: changing the
return type was ok because of tail recursion. On the other hand, method arguments should stay
as they are.

### Hiding the container in a context

If we were in Scala, another valuable option would be to provide an implicit parameter for
`_fibonacci` containing a context:
```
public interface MethodExecutionContext {

    ArgsContainer getArgsContainer();

    void setArgsContainer(ArgsContainer argsContainer);

}
```
The advantage of implicit parameters is that we may skip to explicitly call them
on function call, hence it may be a quite good compromise between code alteration and ease of
use. We could debate hours about that but the point is: there's no such thing as
implicit parameters in Java.

Another option would be to put the context in a static variable somewhere or, better,
in some concurrent map `Map<Thread, MethodExecutionContext>`. After all: one recursive call
will be executed by one thread, and one thread will execute only one recursive call at a time.

Nevertheless, this is not the way we have chosen to follow. Rather, we are going to
create a context as a Thread. This technique is not new and is widely used in Spring applications,
for example, where a Thread typically contains security information: the context of the HTTP
request being performed. Same story here: we are going to enclose our method call in
a context provider thread:
```
public interface WithMethodExecutionContext {
    MethodExecutionContext getMethodExecutionContext();
}
class ContextThread extends Thread implements WithMethodExecutionContext {
    private final MethodExecutionContext ctx; // I let you guess the impl of this one...
    ContextThread(Runnable r) {
        super(r);
    }

    @Override public MethodExecutionContext getMethodExecutionContext() {
        return ctx;
    }
}
```

### Using the `@TailRecursiveExecutor` as a gateway

The public `fibonacci` method will be used as a gateway for our recursive call.
So far, we used it to perform checks on the arguments, and cast back the result.
Maybe it may also realize some cleaning operation for the end user,
or a last conversion for a better return type.

All these checks are "business checks", relevant for the end user and full of
business logic. We should not change that; but we should enclose our
method in such a way that it prepare the execution context for us.

First it should be able to check that the current Thread is indeed an execution context:
```
public final void assertLegitAccess() {
    if (Thread.currentThread() instanceof WithMethodExecutionContext) setupContext();
    else throw new IllegalStateException("Unable to launch TailRecursive method without an execution context provider");
}
```
It also should be able to initialize the context (and finalize it) with a arguments-container:
```
public final void relaxStorage() {
    try {
        getCurrentContext().setArgsContainer(null);
    } catch(RuntimeException ignored) {}
}
void setupContext() {
    getCurrentContext().setArgsContainer(ArgsContainer.getInstance());
}
private MethodExecutionContext getCurrentContext() {
    return ((WithMethodExecutionContext) Thread.currentThread()).getMethodExecutionContext();
}
```
Last but not least, it should be able to wait for a method call to finish,
and return the result. All these facilities are relevantly grouped in an abstract class:
```
public abstract class ContextStorageTemplate {

    public final void assertLegitAccess() {
        if (Thread.currentThread() instanceof WithMethodExecutionContext) setupContext();
        else throw new IllegalStateException("Unable to launch TailRecursive method without an execution context provider");
    }

    public final void relaxStorage() {
        try {
            getCurrentContext().setArgsContainer(null);
        } catch(RuntimeException ignored) {}
    }
    
    public final void setupContext() {
        getCurrentContext().setArgsContainer(ArgsContainer.getInstance());
    }

    public @FunctionalInterface interface MethodCall {
        Object call() throws Throwable;
    }
    public final Object awaitForResult(MethodCall methodCall) throws Throwable {
        ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
        new ContextThread(() -> {
            setupContext();
            try {
                queue.offer(methodCall.call());
            } catch(Throwable error) {
                queue.offer(error);
                throw new RuntimeException(error);
            } finally {
                relaxStorage();
            }
        }).start();
        return queue.take();
    }

    private MethodExecutionContext getCurrentContext() {
        return ((WithMethodExecutionContext) Thread.currentThread()).getMethodExecutionContext();
    }

}
```

## Gluing all together with AOP

So far so good, we have sketched the basic algorithms of our tail recursive approach.
Now we need to draw a link between our algorithms and the final `fibonacci` and `_fibonacci`
methods.

They are many ways to do that: we can define those methods together in a custom structure (a class)
that will perform the wrapping for us. This method offers some implementation/usage issues:
first it should act by reflexion to guess the *real method signatures*: so far our algorithms are
expressed via the super `Object` class. Technically, we should have used generics but remember that
the number of arguments (and thus, of generics) used to express `fibonacci` and `_fibonacci` are a
business matter, and we are doing the plumbery here. Also, in Java, primitives cannot be used as
generic types.

Another approach would be to use reflexion, but then we need to be aware of a lot of things
that may not be so easy to get. For example: defining `_fibonacci` as a private method
would require us to turn it accessible for reflexion invoke, which is quite a strong side effect.

Other approaches are based on the use of annotations. In their basic forms, annotations in Java
are only used for code readability. Some of them (like `@Override`, `@FunctionalInterface`) provide
compilation constraints, other (`@SuppressWarning`) compilation warnings indications.
If you want to make your annotations useful for "something", you usually rely on an annotation processor
to generate code for you. This is fine but a bit unstructured.

We have chosen Aspect Oriented Programming to glue our algorithms together.
Aspects are other structures, orthogonal to classes, whose aim is to provide "the same kind of extra
behavior for you business logic". Typically: logging, security context, remote service connection
context, ...

It really sounds like what Java lacks: context! And it also sounds good for us. With AOP, we can define
an aspect that will act on different parts of the code:
```
/* We're using AspectJ for AOP library in Java */
@Aspect
public class TailRecursiveAspect extends JointPointConverter {

    private static final Context context; // implement it yourself
    private static final Strategy strategy; // Implement it yourself

    @Around("@annotation(TailRecursive) && execution(Object *(..))")
    public Object aroundTailrec(ProceedingJoinPoint jp) throws Throwable {
        return strategy.tailRecTrap(jp::proceed, jp::getArgs);
    }

    @Around("@annotation(TailRecursiveExecutor) && execution(* *(..))")
    public Object aroundExecutor(ProceedingJoinPoint jp) throws Throwable {
        return context.awaitForResult(jp::proceed);
    }

}
```
AspectJ syntax may look a bit mystic at first glance. Here is how it goes:
```
@Around("@annotation(TailRecursive) && execution(Object *(..))")
```
basically means: around each execution of a method whose return type is `Object`,
and if the method is annotated with `TailRecursive`. The `ProceedingJoinPoint` may be
understood as the native method that is called (`_fibonacci`).

On any call to `_fibonacci`, it will actually call the `aroundTailrec`
advice (AOP terminology) with `_fibonacci`. Here we can access the arguments via `jp.getArgs()`
and we can also let the process continue via `jp.proceed(...)`. We should provide a return
value for the call. The advice
```
@Around("@annotation(TailRecursive) && execution(Object *(..))")
public Object aroundTailrec(ProceedingJoinPoint jp) throws Throwable {
    return jp.proceed(jp.getArgs());
}
```
would be as efficient as doing nothing: we execute the join point with the expected arguments,
and we return the actual value of the join point. In our case, we redefine both the
arguments and the returning value, using our `tailRecTrap` algorithm.

Same goes for the `TailRecursiveExecutor` advice: it will continue the join point process
in our custom tail-recursive context.

### Letting Maven compile

Debugging advices with AspectJ is, I would say, quite a mess.
As I'm using Maven, I included the following dependency
```

```
together with this Maven plugin:
```
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.7</version>
    <configuration>
        <complianceLevel>1.8</complianceLevel>
        <source>1.8</source>
        <target>1.8</target>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
Pay attention: using lambda expressions or
method references in an Aspect leads to really weird
unmeaningful stacktraces at compile time. You should be aware
of that, because you'll find no clue of this problem in the stack
(which will look more like assembly code that Java one):
```
 static void <clinit>():
                    NEW be.jdevelopment.tailrec.aspect.Context   (line 13)
                    DUP
                    INVOKESPECIAL be.jdevelopment.tailrec.aspect.Context.<init> ()V
                    PUTSTATIC be.jdevelopment.tailrec.aspect.TailRecursiveAspect.context Lbe/jdevelopment/tailrec/aspect/Context;
                    NEW be.jdevelopment.tailrec.aspect.Strategy   (line 14)
                    DUP
                    GETSTATIC be.jdevelopment.tailrec.aspect.TailRecursiveAspect.context Lbe/jdevelopment/tailrec/aspect/Context;
                    INVOKESPECIAL be.jdevelopment.tailrec.aspect.Strategy.<init> (Ljava/util/function/Supplier;)V
                    PUTSTATIC be.jdevelopment.tailrec.aspect.TailRecursiveAspect.strategy Lbe/jdevelopment/tailrec/aspect/Strategy;
                    NOP
    catch java.lang.Throwable -> E0
    |               INVOKESTATIC be.jdevelopment.tailrec.aspect.TailRecursiveAspect.ajc$postClinit ()V
    |               GOTO L0
    catch java.lang.Throwable -> E0
                E0: ASTORE_0
                    ALOAD_0
                    PUTSTATIC be.jdevelopment.tailrec.aspect.TailRecursiveAspect.ajc$initFailureCause Ljava/lang/Throwable;
                L0: RETURN
  end static void <clinit>()
...
```
Finally, beware that your annotations actually have to be
fully qualified:
```
@Around("@annotation(be.jdevelopment.tailrec.strategy.TailRecursive) && execution(Object *(..))")
```
After compilation, our main example
```
@TailRecursiveExecutor
static BigInteger fibonacci(int N) {
    if (N < 0) throw
            new IllegalArgumentException("Negative ranked Fibonacci is not defined");
    if (N == 0 || N == 1)
        return BigInteger.valueOf(1L);
    BigInteger[] stack = {BigInteger.valueOf(1L), BigInteger.valueOf(1L)};
    return (BigInteger) _fibonacci(stack, N - 2);
}

@TailRecursive
static Object _fibonacci(BigInteger[] stack, int remainingIter) {
    if (remainingIter == 0)
        return stack[1];
    BigInteger buff = stack[0];
    stack[0] = stack[1];
    stack[1] = buff.add(stack[0]);
    return _fibonacci(stack, remainingIter - 1);
}
```
look like this:
```
@TailRecursiveExecutor
static BigInteger fibonacci(int N) {
    JoinPoint var3 = Factory.makeJP(ajc$tjp_0, (Object)null, (Object)null, Conversions.intObject(N));
    TailRecursiveAspect var10000 = TailRecursiveAspect.aspectOf();
    Object[] var4 = new Object[]{Conversions.intObject(N), var3};
    return (BigInteger)var10000.aroundExecutor((new Main$AjcClosure1(var4)).linkClosureAndJoinPoint(65536));
}

@TailRecursive
static Object _fibonacci(BigInteger[] stack, int remainingIter) {
    JoinPoint var5 = Factory.makeJP(ajc$tjp_1, (Object)null, (Object)null, stack, Conversions.intObject(remainingIter));
    TailRecursiveAspect var10000 = TailRecursiveAspect.aspectOf();
    Object[] var6 = new Object[]{stack, Conversions.intObject(remainingIter), var5};
    return var10000.aroundTailRec((new Main$AjcClosure3(var6)).linkClosureAndJoinPoint(65536));
}
```
You can guess where the advice was injected, where
casting occur, ...

## Conclusion

We have implemented a tail recursion mechanism inside
Java in an AOP mindset.
(Can any interested guy can provide *real* benchmarks
about speed and memory usage?)

Is tail recursion really useful? Well, in many situations, you
*may not expect* stackoverflow. But usually you cannot guarantee it.
For example, finding cycles in a graph may be done in a tail
recursive way without optimization, and everything will just work
fine! Until the client pushes an too large input...

In thoses cases, you usually have to make a choice:
code readability and maintenance, over no crash (and not just
performances). Usually you sacrifice code style, because the cost
of an error (Note that `StackOverflowError` extends `VirtualMachineError`,
which extends `Error`: **those kind of throwable you should never ever
try to catch**, ever...) is very high.

With a tail recursive approach as we did, you *do not optimize*
your code. Rather, you allow a certain code style (if you hate it,
just continue with streams!) with the following philosophy:
I'm aware that I'm going a bit too much of overhead, but it
worth it as there is no chance for an `Error` too happen.
