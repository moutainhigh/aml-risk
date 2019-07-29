package com.loits.aml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport
public class RiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskApplication.class,args);
    }

    @Bean
    public ProjectionFactory projectionFactory() {
        return new SpelAwareProxyProjectionFactory();
    }

}
