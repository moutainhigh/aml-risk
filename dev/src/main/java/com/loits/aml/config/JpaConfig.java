package com.loits.aml.config;

import com.loits.aml.mt.DataSourceMultiTenantConnectionProvider;
import com.loits.aml.mt.MultiTenantDataSources;
import com.loits.aml.mt.MultiTenantProperties;
import com.loits.aml.mt.TenantIdentifierResolver;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@EnableConfigurationProperties({
    MultiTenantProperties.class, JpaProperties.class, HibernateProperties.class
})
@EnableJpaRepositories(
    basePackages = "com.loits.aml",
    entityManagerFactoryRef = "defaultEntityManager",
    transactionManagerRef = "defaultTransactionManager")
public class JpaConfig {

    private ConfigurableListableBeanFactory beanFactory;

    private HibernateProperties hibernateProperties;

    private JpaProperties jpaProperties;

    private MultiTenantProperties multiTenantProperties;


    public JpaConfig(JpaProperties jpaProperties,
                     HibernateProperties hibernateProperties,
                     MultiTenantProperties multiTenantProperties,
                     ConfigurableListableBeanFactory beanFactory) {
        this.jpaProperties = jpaProperties;
        this.hibernateProperties = hibernateProperties;
        this.multiTenantProperties = multiTenantProperties;
        this.beanFactory = beanFactory;
    }


    @Bean
    public MultiTenantDataSources multiTenantDataSources() {
        MultiTenantDataSources multiTenantDataSources = new MultiTenantDataSources(multiTenantProperties.getDefaultTenantId());
        multiTenantProperties.getDataSources().forEach(
            ds -> multiTenantDataSources.add(ds.getTenantId(), ds.initializeDataSourceBuilder().build()));

        return multiTenantDataSources;
    }


    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider(MultiTenantDataSources multiTenantDataSources) {
        return new DataSourceMultiTenantConnectionProvider(multiTenantDataSources);
    }


    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }


    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean defaultEntityManager(
        MultiTenantConnectionProvider multiTenantConnectionProvider,
        CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setPersistenceUnitName("default");
        em.setPackagesToScan("com.loits.aml");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
        properties.put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory));

        properties.put(AvailableSettings.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.Oracle12cDialect");

        em.setJpaPropertyMap(properties);

        return em;
    }


    @Primary
    @Bean
    public PlatformTransactionManager defaultTransactionManager(LocalContainerEntityManagerFactoryBean defaultEntityManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(defaultEntityManager.getObject());
        return transactionManager;
    }


}

