package com.zhouhc.generator.service;

import com.zhouhc.generator.dto.RequestDTO;

/**
 * 构建代码
 */
public interface DataGeneratorService {
    public byte[] generatorCode(RequestDTO dto);
}
