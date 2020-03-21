package be.jdevelopment.tailrec.lib.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

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
