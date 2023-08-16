package com.zhouhc.generator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaFileDTO {

    private String fullFileName;

    private String fileContent;
}
