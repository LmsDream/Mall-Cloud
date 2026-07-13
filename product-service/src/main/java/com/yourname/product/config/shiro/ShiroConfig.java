package com.yourname.product.config.shiro;

import com.yourname.product.config.shiro.CustomRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    //1、注册JWT Realm
    @Bean
    public JwtRealm jwtRealm(){
        return new JwtRealm();
    }

    //第一步：把realm注册为springBean
    @Bean
    public CustomRealm customRealm(){
        return new CustomRealm();
    }
    //第二步：创建securityManager并注入realm
    //2、注册SecurityManager（禁用Session，无状态）
    @Bean
    public DefaultWebSecurityManager securityManager(){
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(jwtRealm());
        //关闭Shiro自带的session（微服务必须无状态）
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator evaluator = new DefaultSessionStorageEvaluator();
        evaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(evaluator);
        manager.setSubjectDAO(subjectDAO);
        return manager;
    }
    //第三步：配置Shiro过滤器（暂时只放行所有请求，避免干扰）
    //3、Shiro过滤器工厂（配置哪些接口拦截，哪些放行）
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager){
        ShiroFilterFactoryBean factory  = new ShiroFilterFactoryBean();
        factory.setSecurityManager(securityManager);
        //注意：为了测试，先让所有请求都放行，等启动成功在家jwt过滤
        //自定义JWT过滤器
        Map<String, Filter> filters  = new LinkedHashMap<>();
        filters .put("jwt",new JwtFilter());
        factory.setFilters(filters);

        //自定义拦截规则
        Map<String,String> filterChain = new LinkedHashMap<>();
        //登录接口必须放行
        filterChain.put("/api/auth/login","anon");
        //放行 Feign 调用路径（order-service 远程调用）
        filterChain.put("/api/product/get/**","anon");
        filterChain.put("/api/product/deduct/**","anon");
        //所有其他的接口走JWT过滤器
        filterChain.put("/**","jwt");
        factory.setFilterChainDefinitionMap(filterChain);
        return factory;
    }

}
