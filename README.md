# Tail recursion in Java

This project illustrates an example of code generation via AOP
to bring tail recursion in Java.

## Forewords

### Long story short

Fibonacci tail recursive implementation:
```
public class Fibo {

    @TailRecursiveExecutor
    public BigInteger fibonacci(int N) {
        if (N < 0) throw
                new IllegalArgumentException("Negative ranked Fibonacci is not defined");
        if (N == 0 || N == 1)
            return BigInteger.valueOf(1L);
        return (BigInteger) _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
    }

    @TailRecursive
    private Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
        if (remainingIter == 0)
            return current;
        return _fibonacci(current, prev.add(current), remainingIter - 1);
    }

}
```
Unit test:
```
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
```
Then `mvn clean compile test`.

### Is tail recursion useful?

Is tail recursion really useful? Well, in many situations, you
*may not expect* stackoverflows. But usually you cannot guarantee it.
For example, finding cycles in a graph may be done in a tail
recursive way without optimization, and everything will just work
fine! Until the client pushes a too large input...

In those cases, you have to make a choice that goes beyond
the scope of performance:
code readability and maintenance, or safety at runtime.
Usually you sacrifice code style, because the cost
of an error is very high.
Note that `StackOverflowError` extends `VirtualMachineError`,
which extends `Error`: **those kind of throwable you should never ever
try to catch**, ever...

With the aspect approach, we *do not optimize*
our code. Rather, we allow a certain code style (if you hate it,
just don't use it!) with the following philosophy:
"I'm aware that I'm doing a bit too much of overhead, but it's
worth it as it avoids an `Error` to happen."

Further developments are then left to you:
enrich with memoization, improve threading, allow
configuration,...
Be creative!

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
after computing `y := f(start - 1)`, we still need to
 evaluate 
`1 + y` before exiting.

A contrario, the method
```
int f (int start, int stack) {
    if (start == 0) return stack;
    return f(start - 1, stack + 1);
}
```
is tail recursive: when calling recursively `f(., 0)` from a stack,
every other operation has been performed.

Those two implementations are perfect examples of the identity map
`N -> N`.

### Tail recursive pattern

Let us focus on a well-known example: the
Fibonacci numbers. A tail recursive, clean
implementation in Java may be done with
two methods:
```
@TailRecursiveExecutor
BigInteger fibonacci(int N) {
    if (N < 0) throw
            new IllegalArgumentException("Negative ranked Fibonacci is not defined");
    if (N == 0 || N == 1)
        return BigInteger.valueOf(1L);
    return (BigInteger) _fibonacci(BigInteger.valueOf(1L), BigInteger.valueOf(1L), N - 2);
}

@TailRecursive
private BigInteger _fibonacci(BigInteger prev, BigInteger current, int remainingIter) {
    if (remainingIter == 0)
        return current;
    return _fibonacci(current, prev.add(current), remainingIter - 1);
}
```
The tail recursive function is `_fibonacci`. Its access is private, as we do not
trust the user about the input: are they non null? Is it called with (1,1)?
Nor about the
remaining number of iterations: is it non negative?
Rather, we expose a public executor
`fibonnaci`, and we take the responsibility of calling `_fibonacci`.
This is a common
design and a wise good practice...

At this point, the above annotations are defined only *for code
readability*, both with methods as target element types and
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
~~We advertise the reader that we have the bad habit to benchmark/monitor performance using
`currentTimeMillis()` and Windows embedded monitor tool; in other words: no benchmark ;-)~~

Any contribution on speed/memory usage is welcome!

### Code modification

Tail recursion is almost all about code readability. The end user should thus
bring as few modification as possible.
The idea that the end user should feel like he was doing standard Java
was a high priority during the realization of the project.

Eventually, the only modification we propose is to change the
signature
```
@TailRecursive BigInteger _fibonacci(BigInteger prev, BigInteger current, int remainingIter)
```
to
```
@TailRecursive Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter)
```
and in consequence, change accordingly
`(return _fibonacci(stack, N - 2)` to
`return (BigInteger) _fibonacci(stack, N - 2)`.

We think this is no great constraint.
Indeed, the method `_fibonacci` *is expected to be*
tail recursive, hence no operation should be performed on it inside its definition.
Thanks to Java boxing/unboxing, primitives return types are also supported.

In addition, as `_fibonacci` is welcome to stay private, the public `fibonacci` takes
the responsibility of casting, which is no real big deal as we know, as programmers, that
the final result of `_fibonacci` is a `BigInteger` instance.

## Going into the code

### Tail recursive strategy

The major part of the job we'll have to do to avoid a stack overflow is
to break the recursivity. We need to be able to distinguish the two following situations:
1. The result of `_fibonacci` is a *end result*
(`BigInteger`, `Integer`, `Long`, whatever...)
2. The result of `_fibonacci` is a call to itself.

In the second situation, we should bypass the next execution and, instead,
store the arguments somewhere to start again from scratch... Until the arguments
are the ones that lead to case (1).

We thus implement a tail recursive trap, as follows:
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
`RecursiveCallProof`. The idea is that it works as a flag to distinguish between
the return of a business-like object, or a call to the function itself.
The privacy of the flag is guaranteed (at least up to a certain point)
by its private access.

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

The `ArgsContainer` interface required in the above template
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
There is not much to say about the structure of this interface.
In fact, our interest will be in the way of storing such an object.
Indeed, nothing in `_fibonacci` was designed
to provide an arguments-container: the signature of the function is
```
Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter)
```
and none of the two arguments may be tweaked as we did for the `PROOF`, because they
are strongly typed and **we do not want to change that**. Remember: changing the
return type was ok because of tail recursion. On the other hand, method arguments should stay
as they are.

### Hiding the container in a context

If we were in Scala, a valuable option would be to provide an implicit parameter for
`_fibonacci` containing a context:
```
public interface MethodExecutionContext {

    ArgsContainer getArgsContainer();

    void setArgsContainer(ArgsContainer argsContainer);

}

Object _fibonacci(BigInteger prev, BigInteger current, int remainingIter)
                 (implicit ctx: MethodExecutionContext)  --> scala syntax
```
This may be a nice compromise between code readability and
code alteration. (Of course in Scala you already have tail recursion...)
However, no such thing exists in Java.

Another option would be to put the context in a static variable somewhere or, better,
in some concurrent map `Map<Thread, MethodExecutionContext>`. After all: one recursive call
will be executed by one thread, and one thread will execute only one recursive call at a time.
This leads to questions about concurrency, dead references, ...

We have chosen another approach. We 
create *a context as a Thread*. This technique is not new and is widely used in Spring applications,
for example. Same story here: we are going to enclose our method call in
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
method in such a way that it prepares the execution context for us.

First it should be able to check that the current thread has indeed an execution context:
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
        if (!(Thread.currentThread() instanceof WithMethodExecutionContext))
        throw new IllegalStateException("Unable to launch TailRecursive method without an execution context provider");
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

There are many ways to do that: we could combine those methods together in a custom structure (a class)
that will perform the wrapping for us. This would lead to some implementation/usage issues:
first it would act by reflexion to guess the *real method signatures*. Furthermore, we would have to use generics.
 We recall that the number of arguments (and thus, of generics) used to express `fibonacci` and `_fibonacci` are
business matter, and we are doing the plumbery here. Also, in Java, primitives cannot be used as
generic types.

Another approach would be to use reflexion, but then we would need to be aware of a lot of things
that may not be so easy to get. For example: defining `_fibonacci` as a private method
would require us to turn it accessible for reflexion invoke, which is quite a strong side effect.

Other approaches are based on the use of annotations. In their basic forms, annotations in Java
are only used for code readability. Some of them (like `@Override`, `@FunctionalInterface`) provide
compilation constraints, other (`@SuppressWarning`) compilation warnings indications.
If you want to make your annotations useful for "something", you usually rely on an annotation processor
to generate code for you. This is fine but a bit unstructured.

We have chosen Aspect Oriented Programming to glue our algorithms together.
Aspects are other structures, orthogonal to classes, whose aim is to provide "implementation of cross-cutting concerns".
 Typically: logging, security context, remote service connection
context, ...

It really sounds like what Java lacks: context! And it also sounds good for us. With AOP, we can define
an aspect that will act at different moments of execution:
```
@Aspect
public class TailRecursiveAspect extends JointPointConverter {

    private static final Context context = new Context();
    private static final Strategy strategy = new Strategy(context);

    /* Pointcuts */

    @Pointcut("@annotation(be.jdevelopment.tailrec.lib.strategy.TailRecursive) && execution(Object *(..))")
    public void tailRecursiveCallPointcut() {}

    @Pointcut("@annotation(be.jdevelopment.tailrec.lib.threading.TailRecursiveExecutor) && execution(* *(..))")
    public void tailRecursiveExecutionPointcut() {}

    @Pointcut("@annotation(be.jdevelopment.tailrec.lib.threading.TailRecursiveEntry) && execution(* *(..))")
    public void tailRecursiveGatewayPointcut() {}

    /* Advices */

    @Around("tailRecursiveCallPointcut()")
    public Object aroundTailRecAdvice(ProceedingJoinPoint jp) throws Throwable {
        return strategy.tailRecTrap(asStrategyMethodCall(jp), asArgProvider(jp));
    }

    @Around("tailRecursiveExecutionPointcut()")
    public Object aroundExecutorAdvice(ProceedingJoinPoint jp) throws Throwable {
        return context.awaitForResult(asCtxMethodCall(jp));
    }

    @Before("tailRecursiveGatewayPointcut()")
    public void beforeEntryAdvice(JoinPoint jp) {
        context.assertLegitAccess();
        context.setupContext();
    }

    @After("tailRecursiveGatewayPointcut()")
    public void afterEntryAdvice(JoinPoint jp) {
        context.relaxStorage();
    }

}
```
Since Java does not natively support AOP, we rely on an
external library called AspectJ. FYI: Spring AOP module
 also relies on AspectJ.
 
AOP syntax may look a bit mystic at first glance. The idea of AOP
is to define pointcuts (= points in the code) and advices to apply
on those pointcuts.

For example, the pointcut
```
@Pointcut("@annotation(be.jdevelopment.tailrec.lib.strategy.TailRecursive) && execution(Object *(..))")
public void tailRecursiveCallPointcut() {}
```
refers to any execution of a function annotated by `TailRecursive`
(fully qualified name is required for AspectJ), with any name and any
number/type of arguments, but whose return type is `Object`.

A call to `_fibonacci` satisfies this definition, as soon as the return
type is not `BigInteger`. This is for your protection, because if we
don't, our aspect will try to perform and cast the result to `BigInteger`,
which will lead to a `ClassCastException` because of our strategy
(remember the `PROOF`?).

Around this pointcut, we define an advice:
```
@Around("tailRecursiveCallPointcut()")
```
The advice catches the method that is called in the `ProceedingJoinPoint` parameter.

On any call to `_fibonacci`, it will actually call the `aroundTailRedAdvice`
advice with `_fibonacci` as join point. Here we can access the arguments via `jp.getArgs()`
and we can also let the process continue via `jp.proceed(...)`. We should provide a return
value for the call. The advice
```
@Around("...")
public Object foo(ProceedingJoinPoint jp) throws Throwable {
    return jp.proceed(jp.getArgs());
}
```
would be as efficient as doing nothing: we execute the join point with the expected arguments,
and we return the actual value of the join point. In our case, we redefine both the
arguments and the returning value, using our `tailRecTrap` algorithm.

Same goes for the `TailRecursiveExecutor` advice: it will continue the join point process
in our custom tail-recursive context.

### Letting Maven compile (personnal feedback)

Debugging advices with AspectJ is, I would say, quite a mess.
As I'm using Maven, I included the following dependency
```
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.8.2</version>
</dependency>
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
                    NEW be.jdevelopment.tailrec.lib.aspect.Context   (line 13)
                    DUP
                    INVOKESPECIAL be.jdevelopment.tailrec.lib.aspect.Context.<init> ()V
                    PUTSTATIC be.jdevelopment.tailrec.lib.aspect.TailRecursiveAspect.context Lbe/jdevelopment/tailrec/aspect/Context;
                    NEW be.jdevelopment.tailrec.lib.aspect.Strategy   (line 14)
                    DUP
                    GETSTATIC be.jdevelopment.tailrec.lib.aspect.TailRecursiveAspect.context Lbe/jdevelopment/tailrec/aspect/Context;
                    INVOKESPECIAL be.jdevelopment.tailrec.lib.aspect.Strategy.<init> (Ljava/util/function/Supplier;)V
                    PUTSTATIC be.jdevelopment.tailrec.lib.aspect.TailRecursiveAspect.strategy Lbe/jdevelopment/tailrec/aspect/Strategy;
                    NOP
    catch java.lang.Throwable -> E0
    |               INVOKESTATIC be.jdevelopment.tailrec.lib.aspect.TailRecursiveAspect.ajc$postClinit ()V
    |               GOTO L0
    catch java.lang.Throwable -> E0
                E0: ASTORE_0
                    ALOAD_0
                    PUTSTATIC be.jdevelopment.tailrec.lib.aspect.TailRecursiveAspect.ajc$initFailureCause Ljava/lang/Throwable;
                L0: RETURN
  end static void <clinit>()
...
```
Finally, beware that your annotations actually have to be
fully qualified:
```
@Around("@annotation(be.jdevelopment.tailrec.lib.strategy.TailRecursive) && execution(Object *(..))")
```