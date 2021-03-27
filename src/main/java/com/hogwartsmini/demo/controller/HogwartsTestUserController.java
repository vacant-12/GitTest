package com.hogwartsmini.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.hogwartsmini.demo.common.ResultDto;
import com.hogwartsmini.demo.common.ServiceException;
import com.hogwartsmini.demo.dao.AddHogwartsTestUserDto;
import com.hogwartsmini.demo.dao.UpdateHogwartsTestUserDto;
import com.hogwartsmini.demo.dto.BuildDto;
import com.hogwartsmini.demo.dto.UserDto;
import com.hogwartsmini.demo.entity.HogwartsTestUser;
import com.hogwartsmini.demo.service.HogwartsTestUserService;
import com.hogwartsmini.demo.util.JenkinsUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.util.StringUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Api(tags = "霍格沃兹测试学院-测试任务管理")
@RestController
@RequestMapping("hogwarts")
@Slf4j    //打出异常日志，这里有点问题
public class HogwartsTestUserController {

    @Autowired
    public HogwartsTestUserService hogwartsTestUserService;
    @Value("${hogwarts.key1}")
    private String hogwartsKey1;
    //@RequestMapping(value = "login", method = RequestMethod.POST)

    @ApiOperation(value = "登录接口")
    @PostMapping("login")
    public ResultDto<UserDto> login(@RequestBody UserDto userDto){
        String result = hogwartsTestUserService.login(userDto);

        //抛出异常

            if(userDto.getName().contains("error2")){
                throw new  NullPointerException();
            }
            if(userDto.getName().contains("error")){
                ServiceException.throwEx("用户名中含有error");
            }


            return ResultDto.success("成功 " + result + "  hogwartsKey1 = " + hogwartsKey1,userDto);
        //return "成功 " + result + "  hogwartsKey1 = " + hogwartsKey1;
    }

    @ApiOperation("注册接口")
    @PostMapping("register")
    public ResultDto<HogwartsTestUser>  register(@RequestBody AddHogwartsTestUserDto addHogwartsTestUserDto){
        HogwartsTestUser hogwartsTestUser = new HogwartsTestUser();
        //使用spring的bean方法简化参数赋值
        BeanUtils.copyProperties(addHogwartsTestUserDto,hogwartsTestUser);
        if(StringUtils.isEmpty(addHogwartsTestUserDto.getPassword())){
            return ResultDto.fail("密码不能为空");
        }
        //System.out.println(JSONObject.toJSONString(hogwartsTestUser));
        log.info("用户注册 请求入参" + JSONObject.toJSONString(hogwartsTestUser));

        //hogwartsTestUserService.save(hogwartsTestUser);
        return hogwartsTestUserService.save(hogwartsTestUser);
    }

    @ApiOperation("用户信息修改接口")
    @PostMapping("update")
    public ResultDto<HogwartsTestUser>  update(@RequestBody UpdateHogwartsTestUserDto updateHogwartsTestUserDto){
        HogwartsTestUser hogwartsTestUser = new HogwartsTestUser();
        //使用spring的bean方法简化参数赋值
        BeanUtils.copyProperties(updateHogwartsTestUserDto,hogwartsTestUser);
        if(StringUtils.isEmpty(updateHogwartsTestUserDto.getPassword())){
            return ResultDto.fail("密码不能为空");
        }
        //System.out.println(JSONObject.toJSONString(hogwartsTestUser));
        log.info("用户修改 请求入参" + JSONObject.toJSONString(hogwartsTestUser));

        //hogwartsTestUserService.save(hogwartsTestUser);
        return hogwartsTestUserService.update(hogwartsTestUser);
    }


    @RequestMapping(value = "byId/{userId}", method = RequestMethod.GET)
    public String getById(@PathVariable("userId") Long userId){
        System.out.println("userId " + userId);
        return "成功 " + userId;
    }

    //@RequestMapping(value = "byId", method = RequestMethod.GET)
    @GetMapping("byId")
    public String getById2(@RequestParam("userId") Long userId,@RequestParam("Id") Long Id){
        System.out.println("RequestParam userId " + userId);
        System.out.println("RequestParam Id " + Id);
        return "成功 " + userId + " Id= " + Id;
    }

    @ApiOperation("用户信息查询接口")
    @GetMapping("byName")
    public ResultDto<List<HogwartsTestUser>> getByName(@RequestParam(value = "userId", required = false)  Integer userId, @RequestParam(value = "userName", required = false)  String userName){
        HogwartsTestUser hogwartsTestUser = new HogwartsTestUser();
        hogwartsTestUser.setId(userId);
        hogwartsTestUser.setUserName(userName);

        log.info("用户信息查询 请求入参" + JSONObject.toJSONString(hogwartsTestUser));

        return hogwartsTestUserService.getByName(hogwartsTestUser);
    }


    @ApiOperation("用户信息删除接口")
    @DeleteMapping("{userId}")
    public ResultDto delete(@PathVariable("userId") Integer userId){

        log.info("用户信息删除 id= " + JSONObject.toJSONString(userId));
        return hogwartsTestUserService.delete(userId);
    }

    @ApiOperation("调用Jenkins构建job")
    @PutMapping("build")
    public ResultDto build(@RequestBody BuildDto buildDto) throws IOException, URISyntaxException {

        log.info("调用Jenkins构建job 请求入参 " + JSONObject.toJSONString(buildDto));
        JenkinsUtil.build(buildDto.getJobName(), buildDto.getUserId(), buildDto.getRemark(), buildDto.getTestCommand());


        return ResultDto.success("成功");
    }

}
