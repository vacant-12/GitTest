package com.hogwartsmini.demo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value ="Jenkins",description = "请求类")
@Data  //使用lombok简化get、set方法
public class BuildDto {
    @ApiModelProperty(value="job名称", example="hogwarts",required=true)
    private String jobName;
    @ApiModelProperty(value ="Jenkins id",example = "String",required = true)
    private String userId;
    @ApiModelProperty(value ="remark",example = "String&Number",required = true)
    private String remark;
    @ApiModelProperty(value ="testCommand",example = "pwsd",required = true)
    private String testCommand;
}
