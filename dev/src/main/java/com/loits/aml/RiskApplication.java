package com.loits.aml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

@EnableCircuitBreaker
@SpringBootApplication
@EnableHystrixDashboard
@EnableAutoConfiguration
public class RiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskApplication.class,args);
    }

    @Bean
    public ProjectionFactory projectionFactory() {
        return new SpelAwareProxyProjectionFactory();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer props = new PropertySourcesPlaceholderConfigurer();
        props.setLocations(new ClassPathResource("service-ref.properties"));
        return props;
    }
}