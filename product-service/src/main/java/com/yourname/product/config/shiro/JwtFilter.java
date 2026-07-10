package com.yourname.product.config.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtFilter extends BasicHttpAuthenticationFilter {

    protected  boolean isAccessAllowed(ServletRequest request, ServletResponse response,Object mappedValue){

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("Authorization");
        if (token !=null && token.startsWith("Bearer")){
            token = token.substring(7);//去掉“Bearer”前缀
            try {
                //提交给Shiro进行认证
                getSubject(request,response).login(new JwtToken(token));
                return true;
            } catch (AuthenticationException e) {
                return false;
            }
        }
        return false;
    }

    //处理OPTIONS请求（跨域预检）
    protected boolean preHandle(ServletRequest request,ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse  = (HttpServletResponse) response;
        if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())){
            httpResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request,response);
    }



}
