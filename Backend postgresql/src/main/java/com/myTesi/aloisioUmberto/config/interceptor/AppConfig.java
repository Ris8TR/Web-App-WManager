package com.myTesi.aloisioUmberto.config.interceptor;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor interceptor;
    //list of endpoint with rate limiter (Bucket)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/v1/**")
                //.addPathPatterns("/v1/users/all-users")
                ;

    }
}
