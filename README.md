# many-datasource
基于springboot2.0.3+aop读写分离
一、最近项目中准备实现对数据库的读写分离，在网上看了大量的文章，没几个是写清楚了的，最后查阅了一些资料才把这个懂弄好，废话不多说，上代码。
二、首先通过springboot快速创建一个maven工程，下面是我的pom文件

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.scj.beilu</groupId>
    <artifactId>many-datasource</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>many-datasource</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.0.0</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>2.6.1</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.google.http-client</groupId>
            <artifactId>google-http-client</artifactId>
            <version>1.19.0</version>
        </dependency>
        <!-- 引入aop支持 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```
这里有两个比较重要的依赖，一个是HikariCP连接池，在spring5后，好像是默认的这个连接池，这里用那个连接池都可以，为了方便，我就用的默认的，而且这个连接池的配置也比阿里巴巴的druid连接池配置简单了很多，想更多了解的同学自行了解了。第二个比较重要的是**spring-boot-starter-aop**这个是引入aop支持，如果不引入这个，当你通过注解的方式向切入是不行的，必须要。
三、创建多数据源，配置application.properties文件

```
## 主数据库配置
spring.datasource.master.name=master
spring.datasource.master.jdbc-url=jdbc:mysql://127.0.0.1:3306/master?useUnicode=true&characterEncoding=utf8
spring.datasource.master.username=root
spring.datasource.master.password=root
spring.datasource.master.driver-class-name=com.mysql.cj.jdbc.Driver
## 从数据库配置
spring.datasource.slave.name=slave
spring.datasource.slave.jdbc-url=jdbc:mysql://127.0.0.1:3306/slaver?useUnicode=true&characterEncoding=utf8
spring.datasource.slave.username=root
spring.datasource.slave.password=root
spring.datasource.slave.driver-class-name=com.mysql.cj.jdbc.Driver
## Hikari连接池的设置
#最小连接
spring.datasource.hikari.minimum-idle=5
#最大连接
spring.datasource.hikari.maximum-pool-size=15
#自动提交
spring.datasource.hikari.auto-commit=true
#最大空闲时常
spring.datasource.hikari.idle-timeout=30000
#连接池名
spring.datasource.hikari.pool-name=DatebookHikariCP
#最大生命周期
spring.datasource.hikari.max-lifetime=900000
#连接超时时间
spring.datasource.hikari.connection-timeout=15000
#心跳检测
spring.datasource.hikari.connection-test-query=SELECT 1

## mybatis配置
#xml路径
mybatis.mapper-locations=classpath:mapper/*.xml
```
这里的数据库配置根据个人情况定，我这里用的是一个数据库服务器中的两个数据库来模拟的不同数据源。
master的sql文如下

```

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `student`
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of student
-- ----------------------------

```
slave的sql文如下

```
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `student`
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of student
-- ----------------------------
```
接下来就要配置多数据源了，新建一个DataSourceConfig.java,这里主要是根据配置文件的数据库配置创建不同的数据源

```
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

```
然后数据源是有了，但是在开发的时候需要切换数据源怎么办呢，这里spring有个AbstractRoutingDataSource通过这个来做路由切换代码如下：

```
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

```
然后这个是根据你传进来的参数做的切换，新建DataSourceContextHolder.java

```
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

```
到这里，你的多数据源配置已经配置完了，但是怎么切换呢，这里切换主要是通过spring aop来做处理，
首先新建TargetDataSource

```
package com.scj.beilu.manydatasource.commons;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource {
    String name() default "master";
}

```
然后新建一个切面DynamicDataSourceAspect.java

```
package com.scj.beilu.manydatasource.commons;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @Description TODO
 * @Author shen
 * @Date 2019/3/27 22:37
 **/
@Aspect
@Order(-1)// 保证该AOP在@Transactional之前执行
@Component
public class DynamicDataSourceAspect {

    @Before("@annotation(ds)")
    public void beforeSwitchDS(JoinPoint point,TargetDataSource ds){
        //获得当前访问的class
        Class<?> className = point.getTarget().getClass();
        //获得访问的方法名
        String methodName = point.getSignature().getName();
        //得到方法的参数的类型
        Class[] argClass = ((MethodSignature)point.getSignature()).getParameterTypes();
        String dataSource = DataSourceContextHolder.DEFAULT_DS;
        try {
            // 得到访问的方法对象
            Method method = className.getMethod(methodName, argClass);
            // 判断是否存在@DS注解
            if (method.isAnnotationPresent(TargetDataSource.class)) {
                TargetDataSource annotation = method.getAnnotation(TargetDataSource.class);
                // 取出注解中的数据源名
                dataSource = annotation.name();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 切换数据源
        DataSourceContextHolder.setDB(dataSource);
    }

    @After("@annotation(ds)")
    public void afterSwitchDS(JoinPoint point,TargetDataSource ds){
        DataSourceContextHolder.clearDB();
    }
}

```
这个切面主要是说在方法处理之前去获取传来过来的参数，并做数据源的切换，然后@Order(-1)这个注解是为了让这个切面是在@Transaction之后运行，其实也可以不应，因为@Transaction注解的顺序是最低的，也就是最后才执行的，反正有事务处理的同学要注意一下这点。
四、数据源也配好了，切面也写好了。那怎么用呢？
当然是用在service层上了

```
@Service
public class ManyService {

    @Autowired
    ManyMapper manyMapper;

    @TargetDataSource
    public List<Student> getStudent() {
        return manyMapper.getStudent();
    }

    @TargetDataSource(name = "slave")
    public int insertStudent(String name) {
        return manyMapper.insertStudent(name);
    }
}
```
@TargetDataSource不带参数时这里设置了默认的数据源，当你访问这两个不同的方法时，实际是访问的不同的数据源。
最后在你的启动类要加上注解

```
@Import(DataSourceConfig.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
```
第一个是导入我们配置的多数据源，第二个是去掉springboot默认的数据源。到此读写分离完成。
github源码地址：
谢谢各位同学去star 和fork一下，有时间也会写一些springcloud的笔记，都是些自己的理解，还有平时踩的坑。
