package com.tsdata.model.factor.main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;  
/** 
 * 注解@SpringBootApplication指定项目为springboot，由此类当作程序入口,自动装配 web 依赖的环境; 
 * @author admin 
 * 
 */  
@SpringBootApplication
@ComponentScan("com.tsdata.model.factor")
public class Application {  
    // 在main方法中启动一个应用，即：这个应用的入口  
    public static void main(String[] args) {  
        // TODO Auto-generated method stub  
        SpringApplication.run(Application.class, args);  
    }  
}  