package com.scj.beilu.manydatasource.commons;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 配置多数据源
 * @Author shen
 * @Date 2019/3/28 0:50
 **/
@Configuration
public class DataSourceConfig {

    //数据源1
    @Bean(name = "master")
    @ConfigurationProperties(prefix = "spring.datasource.master") // application.properteis中对应属性的前缀
    public DataSource dataSource1() {
        return DataSourceBuilder.create().build();
    }

    //数据源2
    @Bean(name = "slave")
    @ConfigurationProperties(prefix = "spring.datasource.slave") // application.properteis中对应属性的前缀
    public DataSource dataSource2() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 动态数据源: 通过AOP在不同数据源之间动态切换
     * @return
     */
    @Primary
    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        // 默认数据源
        dynamicDataSource.setDefaultTargetDataSource(dataSource1());
        // 配置多数据源
        Map<Object, Object> dsMap = new HashMap();
        dsMap.put("master", dataSource1());
        dsMap.put("slave", dataSource2());

        dynamicDataSource.setTargetDataSources(dsMap);
        return dynamicDataSource;
    }

    /**
     * 配置@Transactional注解事物
     * @return
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dynamicDataSource());
    }
}
