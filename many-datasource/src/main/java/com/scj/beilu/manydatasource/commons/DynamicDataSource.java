package com.scj.beilu.manydatasource.commons;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @Description 路由切换
 * @Author shen
 * @Date 2019/3/27 22:37
 **/
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDB();
    }

}
