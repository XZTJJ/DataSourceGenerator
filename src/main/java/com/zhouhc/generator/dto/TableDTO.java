package com.zhouhc.generator.dto;

import lombok.Data;

import java.util.List;

//表信息
@Data
public class TableDTO {
    //表的名称
    private String tableName;
    //表的备注
    private String comments;
    //表的主键
    private ColumnDTO primaryKey;
    //表的列名(不包含主键)
    private List<ColumnDTO> allColumns;
    //类名
    private String className;
}
