package space.kuikui.oj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import space.kuikui.oj.interceptor.LoginInterceptor;
import space.kuikui.oj.service.RedisSetTokenExample;
import space.kuikui.oj.common.JwtLoginUtils;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 注入 RedisSetTokenExample 和 JwtLoginUtils
    private final RedisSetTokenExample redisSetTokenExample;
    private final JwtLoginUtils jwtLoginUtils;

    public WebConfig(RedisSetTokenExample redisSetTokenExample, JwtLoginUtils jwtLoginUtils) {
        this.redisSetTokenExample = redisSetTokenExample;
        this.jwtLoginUtils = jwtLoginUtils;
    }

    // 将 LoginInterceptor 配置为 Bean
    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor(redisSetTokenExample, jwtLoginUtils);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns( "/", "/v3/api-docs", "/static/**");
    }
}