package com.estudos.slowquerydetector.config;

import com.estudos.slowquerydetector.interceptor.SlowQueryInspector;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer slowQueryStatementInspectorCustomizer(SlowQueryInspector inspector) {
        return properties -> properties.put("hibernate.session_factory.statement_inspector", inspector);
    }
}
