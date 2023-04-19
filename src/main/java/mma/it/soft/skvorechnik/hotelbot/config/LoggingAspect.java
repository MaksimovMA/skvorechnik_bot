package mma.it.soft.skvorechnik.hotelbot.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {}

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerMethods() {}

    // Добавьте эту строку для логирования классов с аннотацией @Repository
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryMethods() {}

    // Добавьте эту строку для логирования класса HotelBot
    @Pointcut("within(mma.it.soft.skvorechnik.hotelbot.HotelBot)")
    public void hotelBotMethods() {}

    @Before("serviceMethods() || controllerMethods() || repositoryMethods() || hotelBotMethods()")
    public void logMethodCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("Entering method: {} in class: {}", methodName, className);
    }

    @AfterReturning(pointcut = "serviceMethods() || controllerMethods() || repositoryMethods() || hotelBotMethods()", returning = "result")
    public void logMethodCallReturn(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.info("Exiting method: {} in class: {} with result: {}", methodName, className, result);
    }
}