package com.hogwartsmini.demo.common;

import jdk.internal.instrumentation.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Component
@Slf4j
public class DemoInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenDb tokenDb;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("=== preHandle ===");
        log.info("=== request.getRequestURI() ===" + request.getRequestURI());

        //过滤登录与注册接口，不使用token
        String requestURI = request.getRequestURI();
        if(requestURI.equalsIgnoreCase("/hogwarts/login")
                ||requestURI.equalsIgnoreCase("/hogwarts/register")){
            return true;
        }

        String tokenStr = request.getHeader(UserBaseStr.LOGIN_TOKEN);
        if(Objects.isNull(tokenStr)){
            response.setStatus(401);
            ServiceException.throwEx("客户端未传Token");
        }
        if(Objects.isNull(tokenDb.getTokenDto(tokenStr))){
            response.setStatus(401);
            ServiceException.throwEx("用户未登录");
            return false;
        }

        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        log.info("=== postHandle ===");
        log.info("=== request.getRequestURI() ===" + request.getRequestURI());
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        log.info("=== afterHandle ===");
        log.info("=== request.getRequestURI() ===" + request.getRequestURI());
    }
}
