package com.shopbee.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.LoggerFactory;

@Logged
@Interceptor
@Priority(2000)
public class LoggingInterceptor {

    @AroundInvoke
    public Object logMethodInvocation(InvocationContext context) throws Exception {
        long start = System.currentTimeMillis();
        try {
            return context.proceed();
        } catch (Exception e) {
            throw e;
        } finally {
            LoggerFactory.getLogger(context.getTarget().getClass()).debug("Method {} executed, took {} ms", context.getMethod().getName(), (System.currentTimeMillis() - start));
        }
    }
}
