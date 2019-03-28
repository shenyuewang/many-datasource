package com.scj.beilu.manydatasource.commons;

import org.springframework.context.annotation.Configuration;

/**
 * @Description TODO
 * @Author shen
 * @Date 2019/3/28 0:52
 **/
@Configuration
public class DataSourceContextHolder {
    /**
     * 默认数据源
     */
    public static final String DEFAULT_DS = "master";

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    // 设置数据源名
    public static void setDB(String dbType) {
        contextHolder.set(dbType);
    }

    // 获取数据源名
    public static String getDB() {
        return (contextHolder.get());
    }

    // 清除数据源名
    public static void clearDB() {
        contextHolder.remove();
    }
}
