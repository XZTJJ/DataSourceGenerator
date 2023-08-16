package com.zhouhc.generator.controller;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import com.zhouhc.generator.dto.RequestDTO;
import com.zhouhc.generator.service.DataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/code")
public class DataGeneratorController {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @PostMapping("/generator")
    public void code(@RequestBody RequestDTO dto, HttpServletResponse response) throws IOException {
        if (ObjectUtil.isEmpty(dto))
            throw new RuntimeException("生成参数为空");
        if (ObjectUtil.isEmpty(dto.getTables()) || ObjectUtil.isEmpty(dto.getType()))
            throw new RuntimeException("必填项为空");
        byte[] data = dataGeneratorService.generatorCode(dto);
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"generator-code.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IoUtil.write(response.getOutputStream(),false,data);
    }
}
