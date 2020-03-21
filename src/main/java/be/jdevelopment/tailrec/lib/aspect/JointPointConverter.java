package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.strategy.RecursiveStrategyTemplate;
import be.jdevelopment.tailrec.lib.threading.ContextStorageTemplate;
import org.aspectj.lang.ProceedingJoinPoint;

class JointPointConverter {

    ContextStorageTemplate.MethodCall asCtxMethodCall(ProceedingJoinPoint jp) {
        return jp::proceed;
    }

    RecursiveStrategyTemplate.MethodCall asStrategyMethodCall(ProceedingJoinPoint jp) {
        return jp::proceed;
    }

    RecursiveStrategyTemplate.ArgsProvider asArgProvider(ProceedingJoinPoint jp) {
        return jp::getArgs;
    }

}
