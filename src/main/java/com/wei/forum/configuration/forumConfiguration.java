package com.wei.forum.configuration;

import com.wei.forum.interceptor.LoginRequiredInterceptor;
import com.wei.forum.interceptor.PassportInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * @Description //配置拦截器
 * create by weilei on 2018/12/18 10:42
 **/
@Component
public class forumConfiguration extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {
    private static final Logger logger = LoggerFactory.getLogger(forumConfiguration.class);
    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor);
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/user/*");
        super.addInterceptors(registry);
    }
}
