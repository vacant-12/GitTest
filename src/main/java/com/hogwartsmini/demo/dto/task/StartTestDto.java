package com.hogwartsmini.demo.dto.task;

import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value="执行测试任务类",description="请求参数类" )
@Data
public class StartTestDto {

    /**
     * ID
     */
    @ApiModelProperty(value="测试任务id",required=true, example = "112")
    @NotNull
    private Integer taskId;

    /**
     * 执行测试的命令脚本
     */
    @ApiModelProperty(value="执行测试的命令脚本", example = "mvn test")
    private String testCommand;

}