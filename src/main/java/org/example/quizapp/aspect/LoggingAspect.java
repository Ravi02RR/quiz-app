package org.example.quizapp.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger methodLogger = LoggerFactory.getLogger("METHOD_LOGGER");
    private static final Logger exceptionLogger = LoggerFactory.getLogger("EXCEPTION_LOGGER");

    @Around("execution(* org.example.quizapp.service.*.*(..)) || " +
            "execution(* org.example.quizapp.controller.*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        methodLogger.info(">>> Entering method: {}.{}", className, methodName);
        methodLogger.info("    Arguments: {}", Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            methodLogger.info("<<< Exiting method: {}.{}", className, methodName);
            methodLogger.info("    Execution time: {} ms", executionTime);
            methodLogger.info("    Return value: {}", result != null ? result.getClass().getSimpleName() : "null");
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            methodLogger.error("!!! Exception in method: {}.{}", className, methodName);
            methodLogger.error("    Execution time before exception: {} ms", executionTime);
            throw e;
        }
    }

    @AfterThrowing(pointcut = "execution(* org.example.quizapp..*(..))", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        exceptionLogger.error("=== EXCEPTION CAUGHT ===");
        exceptionLogger.error("Class: {}", className);
        exceptionLogger.error("Method: {}", methodName);
        exceptionLogger.error("Exception Type: {}", exception.getClass().getName());
        exceptionLogger.error("Exception Message: {}", exception.getMessage());
        exceptionLogger.error("Stack Trace:", exception);
        exceptionLogger.error("========================");
    }
}
