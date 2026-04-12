package com.draftly.config;

import com.draftly.security.RoleBasedAccessInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration to allow React frontend (port 3000) to call the API (port 8080).
 */
@Configuration
public class WebConfig {

    private final RoleBasedAccessInterceptor roleBasedAccessInterceptor;

    public WebConfig(RoleBasedAccessInterceptor roleBasedAccessInterceptor) {
        this.roleBasedAccessInterceptor = roleBasedAccessInterceptor;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(roleBasedAccessInterceptor)
                        .addPathPatterns("/api/**");
            }
        };
    }
}
