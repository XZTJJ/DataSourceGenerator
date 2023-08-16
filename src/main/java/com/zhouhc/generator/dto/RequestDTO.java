package com.zhouhc.generator.dto;

import lombok.Data;

import java.util.List;

/**
 * 构建请求类
 */
@Data
public class RequestDTO {
    //1:mybatis-plus, 2:mybatis
    private Integer type ;
    //表名
    private List<String> tables;
    //表前缀
    private String tablePrefix;
    //路径
    private String packageName;
    //作者
    private String author;
    //邮件
    private String email;

}
