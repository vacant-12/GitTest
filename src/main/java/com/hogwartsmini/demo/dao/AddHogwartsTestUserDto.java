package com.hogwartsmini.demo.dao;

import com.hogwartsmini.demo.entity.BaseEntityNew;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value ="用户登录类",description = "请求类")
@Data
public class AddHogwartsTestUserDto extends BaseEntityNew {
    @ApiModelProperty(value ="用户名",example = "String",required = true)
    private String userName;
    @ApiModelProperty(value ="密码",example = "String",required = true)
    private String password;
    @ApiModelProperty(value ="邮箱",example = "String")
    private String email;
    @ApiModelProperty(value ="生成用例job名称",example = "String")
    private String autoCreateCaseJobName;
    @ApiModelProperty(value ="执行测试job名称",example = "String")
    private String startTestJobName;
    @ApiModelProperty(value ="Jenkins服务器",example = "String")
    private Integer defaultJenkinsId;
}