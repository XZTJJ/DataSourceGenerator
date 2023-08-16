package com.zhouhc.generator.dto;

import lombok.Data;

@Data
public class ColumnDTO {
    //列名
    private String columnName;
    //列名类型
    private String dataType;
    //列名备注
    private String comments;
    //属性名称(第一个字母小写)
    private String attrName;
    //属性类型
    private String attrType;
    //扩展用
    private String extra;
}
