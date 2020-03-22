package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.ArgsContainer;
import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;
import be.jdevelopment.tailrec.lib.threading.RecursiveContextBinder;
import be.jdevelopment.tailrec.lib.threading.TailRecursiveExecutor;
import be.jdevelopment.tailrec.lib.threading.WithMethodExecutionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class TailRecursiveAspect extends JoinPointConverter {

    /* Pointcuts */

    @Pointcut("@annotation(be.jdevelopment.tailrec.lib.strategy.TailRecursive) && execution(Object *(..))")
    public void tailRecursiveCallPointcut() {}

    @Pointcut("@annotation(tailRecursiveExecutor) && execution(* *(..))")
    public void tailRecursiveExecutionPointcut(TailRecursiveExecutor tailRecursiveExecutor) {}

    /* Advices */

    @Around("tailRecursiveCallPointcut()")
    public Object aroundTailRecAdvice(ProceedingJoinPoint jp) throws Throwable {
        return ThreadBasedStrategy.INSTANCE.tailRecTrap(asStrategyMethodCall(jp), asArgProvider(jp));
    }

    @Around("tailRecursiveExecutionPointcut(tailRecursiveExecutor)")
    public Object aroundExecutorAdvice(ProceedingJoinPoint jp, TailRecursiveExecutor tailRecursiveExecutor) throws Throwable {
        return getContextBinder(tailRecursiveExecutor)
                .bindInContext(asCtxMethodCall(jp));
    }

    /* Implementation specific */

    public void register(String binderKey, RecursiveContextBinder contextBinder) {
        JoinPointConverter.BINDER_REPOSITORY.put(binderKey, contextBinder);
    }

    private static class ThreadBasedStrategy extends RecursiveStrategyTemplate {
        private static final RecursiveStrategyTemplate INSTANCE = new ThreadBasedStrategy();

        @Override protected ArgsContainer getArgsContainer() {
            return ((WithMethodExecutionContext) Thread.currentThread())
                    .getMethodExecutionContext()
                    .getArgsContainer();
        }
    }

}
