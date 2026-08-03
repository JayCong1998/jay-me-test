package com.jaymetest.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import com.jaymetest.admin.AdminStpUtil;
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

    /** 注册 Sa-Token 拦截器（注解模式：仅 @SaCheckLogin / @SaCheckRole 等方法校验登录） */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 后台使用独立 StpLogic，必须先于普通 /api/** 拦截器注册，避免 admin token 被用户态逻辑误判。
        registry.addInterceptor(new SaInterceptor(handle -> AdminStpUtil.checkLogin()))
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");

        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/admin/auth/login",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/classic/**",
                        "/api/questions/**",
                        "/api/game-results",
                        "/api/statistics/overview",
                        "/api/health"
                );
    }
}
