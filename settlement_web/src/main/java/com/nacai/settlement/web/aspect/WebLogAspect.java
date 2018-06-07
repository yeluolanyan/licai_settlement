package com.nacai.settlement.web.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nacai.settlement.web.aspect.rpc.RpcTrackHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Description: 日志拦截记录
 * Created by shan on 2018/5/29.
 */
@Component
@Aspect
public class WebLogAspect {
    private static final Logger log = LoggerFactory.getLogger(WebLogAspect.class);
    private ThreadLocal<Long> elaspedTime = new ThreadLocal<Long>();

    @Pointcut("execution(public * com.nacai.settlement.web.controller.*.*(..))")
    public void webLog(){}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable{
        try {
            elaspedTime.set(System.currentTimeMillis());
            // 接收到请求，记录请求内容
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            // 记录下请求内容
            String trackId = UUID.randomUUID().toString();
            RpcTrackHolder.setTrackId(trackId);
            StringBuilder requestLog = new StringBuilder();
            requestLog.append(">>>>>>接收请求ID: ") .append(trackId).append(" - ");
            requestLog.append(request.getRequestURL()).append(" - ");
            requestLog.append(request.getMethod()).append(" - ");
            requestLog.append(getIpAddr(request));
            log.info(requestLog.toString());
            log.info("请求参数: {}", JSONObject.toJSON(request.getParameterMap()));
        } catch (Exception e) {
            log.error("记录请求日志异常！", e);
        }
    }


    @Around("webLog()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Exception ex) {
            log.error("拦截异常, 请求ID："+ RpcTrackHolder.getTrackId(), ex);
            throw ex;
        } finally {
            long s = elaspedTime.get();
            // 处理完请求，返回内容
            String resultStr = JSON.toJSONString(result == null ? "" : result);
            if (resultStr.length() > 200) {
                resultStr = resultStr.substring(0, 200);
            }
            log.info("<<<<<<响应ID：{}, {}, 耗时: {}ms", new Object[] {RpcTrackHolder.getTrackId(), resultStr, (System.currentTimeMillis() - s)});
        }
    }

    private String getIpAddr(HttpServletRequest request){
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
    }

}
