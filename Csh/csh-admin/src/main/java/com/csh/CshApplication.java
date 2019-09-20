package com.csh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 启动程序
 * 
 * @author csh
 */

@EnableTransactionManagement
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class CshApplication
{
    @Bean
    public Object testBean(PlatformTransactionManager platformTransactionManager){
        System.out.println(">>>>>>>>>>" + platformTransactionManager.getClass().getName());
        return new Object();
    }

    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(CshApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  金海马启动成功   ლ(´ڡ`ლ)ﾞ  \n" );
        System.out.println("------志飞高远-----------成就人生------------");
        System.out.println("世纪海马科技");

    }
    /*@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CshApplication.class);
    }*/
    /*@Override
    extends SpringBootServletInitializer
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CshApplication.class);
    }*/
}