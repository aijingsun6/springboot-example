package cc.alking.example.springboot.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class AccountAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAspect.class);

    @Pointcut("execution(public * cc.alking.example.springboot.service.AccountService.*(..))")
    public void serviceLayer() {
    }

    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        LOGGER.info("[@Before] method = {}, args = {}", methodName, Arrays.toString(args));
    }

    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        LOGGER.info("[@After] method = {}, args = {}", methodName, Arrays.toString(args));
    }
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        LOGGER.info("[@AfterReturning] method = {}, args = {}, result = {}", methodName, Arrays.toString(args), result);
    }
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        LOGGER.info("[@AfterThrowing] method = {}, args = {}", methodName, Arrays.toString(args));
        LOGGER.error("[@AfterThrowing]", ex);
    }

    @Around("serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        long start = System.currentTimeMillis();

        LOGGER.info("[@Around] start, method = {}, args = {}", methodName, Arrays.toString(args));
        Object result = null;
        try {
            // 执行目标方法（必须调用 proceed，否则业务逻辑被跳过）
            result = joinPoint.proceed();
        } catch (Throwable t) {
            // 重新抛出，让上层通知也能捕获
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            LOGGER.info("[@Around] end, cost:" + elapsed + " ms");
        }
        return result;
    }
}
