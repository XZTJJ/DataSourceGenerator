package com.zhouhc.generator.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.zhouhc.generator.dto.ColumnDTO;
import com.zhouhc.generator.dto.JavaFileDTO;
import com.zhouhc.generator.dto.RequestDTO;
import com.zhouhc.generator.dto.TableDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;


import java.io.File;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;


public class DataGeneratorUtil {

    //生成代码的工具类
    public static List<JavaFileDTO> generatorCode(RequestDTO dto, Map<String, String> typeMapping,
                                                  Map<String, String> tableMetaData, List<Map<String, String>> columnsMetaData) {
        //表信息
        TableDTO tableDTO = new TableDTO();
        tableDTO.setTableName(tableMetaData.get("tableName"));
        tableDTO.setComments(tableMetaData.get("tableComment"));
        String className = tableName2JavaName(tableDTO.getTableName(), dto.getTablePrefix());
        tableDTO.setClassName(className);
        //字段列表
        List<ColumnDTO> columns = columnsMetaData.stream().map(columnMetaData -> DataGeneratorUtil.getColumn(columnMetaData, typeMapping))
                .collect(Collectors.toList());
        tableDTO.setAllColumns(columns);
        //处理主键
        Optional<ColumnDTO> columnKey = columnsMetaData.stream()
                .filter(columnMetaData -> "PRI".equalsIgnoreCase(columnMetaData.get("columnKey")))
                .findFirst().map(columnMetaData -> DataGeneratorUtil.getColumn(columnMetaData, typeMapping));
        tableDTO.setPrimaryKey(columnKey.isPresent() ? columnKey.get() : columnKey.orElse(new ColumnDTO()));
        //生成对应的类
        List<JavaFileDTO> javaFiles = getJavaFile(dto, tableDTO);
        return javaFiles;
    }

    //开始生成对应的类
    private static List<JavaFileDTO> getJavaFile(RequestDTO dto, TableDTO tableDTO) {
        List<JavaFileDTO> result = new ArrayList<JavaFileDTO>();
        //模板设置
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        VelocityContext context = new VelocityContext(getVelocityMap(tableDTO, dto));
        //获取模板对象
        List<String> templates = dto.getType() != 1 ? getMybatisTemplates() : getMybatisPlusTemplates();
        //生成模板类
        for (String template : templates) {
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            String fileName = getFileName(template, tableDTO.getClassName(), dto.getPackageName());
            String fileContent = sw.toString();
            result.add(new JavaFileDTO(fileName, fileContent));
        }
        return result;
    }

    //获取文件名称
    private static String getFileName(String template, String className, String packageName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StrUtil.isEmpty(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator;
        }

        if (template.contains("service.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }
        if (template.contains("serviceImpl.java.vm")) {
            return packagePath + "service/impl" + File.separator + className + "ServiceImpl.java";
        }
        if (template.contains("mapper.java.vm")) {
            return packagePath + "mapper" + File.separator + className + "Mapper.java";
        }
        if (template.contains("entity.java.vm")) {
            return packagePath + "entity" + File.separator + className + ".java";
        }
        if (template.contains("entityDTO.java.vm")) {
            return packagePath + "DTO" + File.separator + className + "DTO.java";
        }
        if (template.contains("entityVO.java.vm")) {
            return packagePath + "VO" + File.separator + className + "VO.java";
        }
        if (template.contains("controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }
        if (template.contains("mapper.xml.vm")) {
            return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + className + "Mapper.xml";
        }
        return "";
    }

    //mybatis的模板文件
    private static List<String> getMybatisTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("mybatisTemplates/mapper.xml.vm");
        templates.add("mybatisTemplates/service.java.vm");
        templates.add("mybatisTemplates/serviceImpl.java.vm");
        templates.add("mybatisTemplates/entity.java.vm");
        templates.add("mybatisTemplates/entityDTO.java.vm");
        templates.add("mybatisTemplates/entityVO.java.vm");
        templates.add("mybatisTemplates/mapper.java.vm");
        templates.add("mybatisTemplates/controller.java.vm");
        return templates;
    }

    //mybatis-plus的模板文件
    private static List<String> getMybatisPlusTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("mybatisPlusTemplates/mapper.xml.vm");
        templates.add("mybatisPlusTemplates/service.java.vm");
        templates.add("mybatisPlusTemplates/serviceImpl.java.vm");
        templates.add("mybatisPlusTemplates/entity.java.vm");
        templates.add("mybatisPlusTemplates/entityDTO.java.vm");
        templates.add("mybatisPlusTemplates/entityVO.java.vm");
        templates.add("mybatisPlusTemplates/mapper.java.vm");
        templates.add("mybatisPlusTemplates/controller.java.vm");
        return templates;
    }


    //获取模板对象
    private static Map<String, Object> getVelocityMap(TableDTO tableDTO, RequestDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableDTO.getTableName());
        map.put("comments", tableDTO.getComments());
        map.put("pk", tableDTO.getPrimaryKey());
        map.put("className", tableDTO.getClassName());
        map.put("columns", tableDTO.getAllColumns());
        map.put("package", dto.getPackageName());
        map.put("author", dto.getAuthor());
        map.put("email", dto.getEmail());
        map.put("datetime", DateUtil.formatDateTime(new Date()));
        map.put("requestMapping", tableDTO.getClassName().toLowerCase());
        //类型处理
        Optional<ColumnDTO> hasBigDecimal = tableDTO.getAllColumns().stream()
                .filter(columnDTO -> "BigDecimal".equals(columnDTO.getAttrType())).findAny();
        if(hasBigDecimal.isPresent())
            map.put("hasBigDecimal", true);
        Optional<ColumnDTO> hasLocalDate= tableDTO.getAllColumns().stream()
                .filter(columnDTO -> "LocalDate".equals(columnDTO.getAttrType())).findAny();
        if(hasLocalDate.isPresent())
            map.put("hasLocalDate", true);
        Optional<ColumnDTO> hasLocalDateTime= tableDTO.getAllColumns().stream()
                .filter(columnDTO -> "LocalDateTime".equals(columnDTO.getAttrType())).findAny();
        if(hasLocalDateTime.isPresent())
            map.put("hasLocalDateTime", true);
        return map;
    }


    //生成字段对象
    private static ColumnDTO getColumn(Map<String, String> columnMetaData, Map<String, String> typeMapping) {
        ColumnDTO columnDTO = new ColumnDTO();
        columnDTO.setColumnName(columnMetaData.get("columnName"));
        columnDTO.setDataType(columnMetaData.get("dataType"));
        columnDTO.setComments(columnMetaData.get("columnComment"));
        columnDTO.setExtra(columnMetaData.get("extra"));
        columnDTO.setAttrName(columnName2JavaName(columnDTO.getColumnName()));
        columnDTO.setAttrType(typeMapping.getOrDefault(columnDTO.getDataType(), "unknowType"));
        return columnDTO;
    }


    //字段名驼峰转换，只是处理_,-的情况
    private static String columnName2JavaName(String column) {
        column = StrUtil.toCamelCase(column);
        column = StrUtil.toCamelCase(column, '-');
        return column;
    }

    //表名的驼峰转换，只是处理_,-的情况
    private static String tableName2JavaName(String table, String tablePrefix) {
        if (StrUtil.isEmpty(tablePrefix))
            StrUtil.replace(table, tablePrefix, "");
        table = StrUtil.toCamelCase(table);
        table = StrUtil.toCamelCase(table, '-');
        table = StrUtil.upperFirst(table);
        return table;
    }
}
