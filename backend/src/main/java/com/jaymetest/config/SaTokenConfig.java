package com.jaymetest.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /** 使用 JWT 风格 */
    @Bean
    public StpLogic stpLogic() {
        return new StpLogicJwtForSimple();
    }

    /** 注册 Sa-Token 拦截器 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 注解鉴权由 @SaCheckLogin 处理，
                    // 这里只做拦截器注册，不做额外逻辑
                }))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/questions/**",
                        "/api/stats/submit",
                        "/api/stats/overview",
                        "/api/health"
                );
    }
}
