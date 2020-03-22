package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;
import be.jdevelopment.tailrec.lib.threading.ContextBinderTemplate;
import be.jdevelopment.tailrec.lib.threading.RecursiveContextBinder;
import be.jdevelopment.tailrec.lib.threading.TailRecursiveExecutor;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class JoinPointConverter {

    final static ConcurrentHashMap<String, RecursiveContextBinder> BINDER_REPOSITORY = new ConcurrentHashMap<>();
    private static final RecursiveContextBinder DEFAULT_CTX = new ContextBinder();

    ContextBinderTemplate.MethodCall asCtxMethodCall(ProceedingJoinPoint jp) {
        return jp::proceed;
    }

    RecursiveStrategyTemplate.MethodCall asStrategyMethodCall(ProceedingJoinPoint jp) {
        return jp::proceed;
    }

    RecursiveStrategyTemplate.ArgsProvider asArgProvider(ProceedingJoinPoint jp) {
        return jp::getArgs;
    }

    RecursiveContextBinder getContextBinder(TailRecursiveExecutor tailRecursiveExecutor) {
        return Optional.ofNullable(tailRecursiveExecutor)
                .map(TailRecursiveExecutor::parameter)
                .map(BINDER_REPOSITORY::get)
                .orElse(DEFAULT_CTX);
    }

}
