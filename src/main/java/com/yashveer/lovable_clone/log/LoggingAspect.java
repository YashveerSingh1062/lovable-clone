package com.yashveer.lovable_clone.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Before(
            "execution(* com.yashveer.lovable_clone.controller..*(..)) || " +
                    "execution(* com.yashveer.lovable_clone.service..*(..)) || " +
                    "execution(* com.yashveer.lovable_clone.repository..*(..))"
    )
    public void logMethodEntry(JoinPoint joinPoint) {

        String className =
                joinPoint.getSignature().getDeclaringTypeName();

        String methodName =
                joinPoint.getSignature().getName();

        log.info("Entering: {}.{}", className, methodName);
    }

    @AfterReturning(
            pointcut =
                    "execution(* com.yashveer.lovable_clone.controller..*(..)) || " +
                            "execution(* com.yashveer.lovable_clone.service..*(..)) || " +
                            "execution(* com.yashveer.lovable_clone.repository..*(..))",
            returning = "result"
    )
    public void logMethodExit(
            JoinPoint joinPoint,
            Object result
    ) {

        String className =
                joinPoint.getSignature().getDeclaringTypeName();

        String methodName =
                joinPoint.getSignature().getName();

        log.info(
                "Exiting: {}.{} Return: {}",
                className,
                methodName,
                result
        );
    }
}