package com.scj.beilu.manydatasource;

import com.scj.beilu.manydatasource.commons.DataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DataSourceConfig.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class ManyDatasourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManyDatasourceApplication.class, args);
    }

}
