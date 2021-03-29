package com.hogwartsmini.demo.service.impl;

import com.hogwartsmini.demo.common.*;
import com.hogwartsmini.demo.dao.HogwartsTestUserMapper;
import com.hogwartsmini.demo.dto.UserDto;
import com.hogwartsmini.demo.entity.HogwartsTestUser;
import com.hogwartsmini.demo.service.HogwartsTestUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class HogwartsTestUserServiceImpl implements HogwartsTestUserService {
    @Autowired
    private TokenDb tokenDb;
    @Autowired
    private HogwartsTestUserMapper hogwartsTestUserMapper;
    @Override
    public ResultDto<HogwartsToken> login(UserDto userDto) {
        String userName = userDto.getName();
        String password = userDto.getPwd();
        System.out.println("userDto.getName() " + userName);
        System.out.println("userDto.getPwd() " + password);

        String newPwd = DigestUtils.md5DigestAsHex((UserBaseStr.md5Hex_sign + userName+password).getBytes());
        HogwartsTestUser queryHogwartsTestUser = new HogwartsTestUser();
        queryHogwartsTestUser.setUserName(userName);
        queryHogwartsTestUser.setPassword(newPwd);

        HogwartsTestUser resultHogwartsTestUser = hogwartsTestUserMapper.selectOne(queryHogwartsTestUser);
        if(Objects.isNull(resultHogwartsTestUser)){
            return ResultDto.fail("用户不存在或密码错误");
        }

        HogwartsToken hogwartsToken = new HogwartsToken();
        String tokenStr = DigestUtils.md5DigestAsHex((System.currentTimeMillis() + userName+password).getBytes());
        hogwartsToken.setToken(tokenStr);

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken(tokenStr);
        tokenDto.setUserId(resultHogwartsTestUser.getId());
        tokenDto.setDefaultJenkinsId(resultHogwartsTestUser.getDefaultJenkinsId());
        tokenDto.setUserName(resultHogwartsTestUser.getUserName());

        tokenDb.addTokenDto(tokenStr,tokenDto);

        return ResultDto.success("成功",hogwartsToken);
    }

    @Override
    public ResultDto<HogwartsTestUser> save(HogwartsTestUser hogwartsTestUser) {

        String userName = hogwartsTestUser.getUserName();
        String password = hogwartsTestUser.getPassword();

        //声明一个类的查询属性，然后赋值查询list判断
        HogwartsTestUser queryHogwartsTestUser = new HogwartsTestUser();
        queryHogwartsTestUser.setUserName(userName);
        List<HogwartsTestUser> resultHogwartsTestUserList = hogwartsTestUserMapper.select(queryHogwartsTestUser);

        if(Objects.nonNull(resultHogwartsTestUserList)&&resultHogwartsTestUserList.size()>0){
            return ResultDto.fail("用户名已存在");
        }
        //加密后入库
        String newPwd = DigestUtils.md5DigestAsHex((UserBaseStr.md5Hex_sign + userName+password).getBytes());

        hogwartsTestUser.setPassword(newPwd);
        hogwartsTestUser.setCreateTime(new Date());
        hogwartsTestUser.setUpdateTime(new Date());

        hogwartsTestUserMapper.insertUseGeneratedKeys(hogwartsTestUser);

        //设置密文之后返回密码设置null
        hogwartsTestUser.setPassword(null);

        return ResultDto.success("成功",hogwartsTestUser);
    }

    @Override
    public ResultDto<HogwartsTestUser> update(HogwartsTestUser hogwartsTestUser) {
        hogwartsTestUser.setCreateTime(new Date());
        hogwartsTestUser.setUpdateTime(new Date());
        hogwartsTestUserMapper.updateByPrimaryKeySelective(hogwartsTestUser);
        return ResultDto.success("成功",hogwartsTestUser);
    }

    @Override
    public ResultDto<List<HogwartsTestUser>> getByName(HogwartsTestUser hogwartsTestUser) {
        List<HogwartsTestUser> hogwartsTestUserList = hogwartsTestUserMapper.select(hogwartsTestUser);
        return ResultDto.success("成功",hogwartsTestUserList);
    }

    @Override
    public ResultDto delete(Integer userId) {

        HogwartsTestUser hogwartsTestUser = new HogwartsTestUser();
        hogwartsTestUser.setId(userId);
        hogwartsTestUserMapper.delete(hogwartsTestUser);
        return ResultDto.success("成功");
    }
}
