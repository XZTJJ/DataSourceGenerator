package com.zhouhc.generator.service.impl;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.zhouhc.generator.config.TableConfig;
import com.zhouhc.generator.dto.JavaFileDTO;
import com.zhouhc.generator.dto.RequestDTO;
import com.zhouhc.generator.mapper.DataGeneratorMapper;
import com.zhouhc.generator.service.DataGeneratorService;
import com.zhouhc.generator.util.DataGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@Slf4j
public class DataGeneratorServiceImpl implements DataGeneratorService {

    @Autowired
    private TableConfig tableConfig;
    @Autowired
    private DataGeneratorMapper dataGeneratorMapper;

    //生成对应的代码
    @Override
    public byte[] generatorCode(RequestDTO dto) {
        //填充
        this.fillDefaultValue(dto);
        //字段映射
        Map<String, String> typeMapping = this.getTypeMapping();
        //创建对象
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        try {
            for (String table : dto.getTables()) {
                //对应元数据
                Map<String, String> tableMetaData = dataGeneratorMapper.queryTable(table);
                List<Map<String, String>> columnsMetaData = dataGeneratorMapper.queryColumns(table);
                //生成的文件
                List<JavaFileDTO> javaFileDTOS = DataGeneratorUtil.generatorCode(dto, typeMapping, tableMetaData, columnsMetaData);
                //每个文件逐一压缩
                for (JavaFileDTO javaFile : javaFileDTOS) {
                    zip.putNextEntry(new ZipEntry(javaFile.getFullFileName()));
                    IoUtil.writeUtf8(zip, false,javaFile.getFileContent());
                }
            }
            IoUtil.flush(zip);
        } catch (Exception e) {
            log.error("生成代码错误", e);
        }finally {
            IoUtil.close(zip);
        }
        return outputStream.toByteArray();
    }

    //填充默认值
    private void fillDefaultValue(RequestDTO dto) {
        if (StrUtil.isEmpty(dto.getPackageName()))
            dto.setPackageName(tableConfig.getPackageName());

        if (StrUtil.isEmpty(dto.getAuthor()))
            dto.setAuthor(tableConfig.getAuthor());

        if (StrUtil.isEmpty(dto.getEmail()))
            dto.setEmail(tableConfig.getEmail());

        if (StrUtil.isEmpty(dto.getTablePrefix()))
            dto.setTablePrefix(tableConfig.getTablePrefix());
    }

    //获取映射类型
    private Map<String, String> getTypeMapping() {
        return tableConfig.getTypeMapping().stream().collect(
                Collectors.toMap(type -> type.split("=")[0], type -> type.split("=")[1], (o, n) -> o));
    }


}
