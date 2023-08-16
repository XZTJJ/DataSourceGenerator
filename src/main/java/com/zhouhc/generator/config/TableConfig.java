package com.zhouhc.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("table.config")
public class TableConfig {
    //包名
    private String packageName;
    //作者
    private String author;
    //邮件
    private String email;
    //表前缀
    private String tablePrefix;
    //类型映射
    private List<String> typeMapping;

}
