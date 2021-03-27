package com.hogwartsmini.demo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value ="用户登录类",description = "请求类")
@Data  //使用lombok简化get、set方法
public class UserDto {
    @ApiModelProperty(value ="用户名",example = "String",required = true)
    private String name;
    @ApiModelProperty(value ="密码",example = "String&Number",required = true)
    private String pwd;

/*
    *
     *     public String getName() {
     *         return name;
     *     }
     *
     *     public void setName(String name) {
     *         this.name = name;
     *     }
     *
     *     public String getPwd() {
     *         return pwd;
     *     }
     *
     *     public void setPwd(String pwd) {
     *         this.pwd = pwd;
     *     }
*/

}
