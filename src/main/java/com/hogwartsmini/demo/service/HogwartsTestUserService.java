package com.hogwartsmini.demo.service;

import com.hogwartsmini.demo.common.ResultDto;
import com.hogwartsmini.demo.dto.UserDto;
import com.hogwartsmini.demo.entity.HogwartsTestUser;

import java.util.List;


public interface HogwartsTestUserService {
    public String login(UserDto userDto);
    public ResultDto<HogwartsTestUser> save(HogwartsTestUser hogwartsTestUser);
    public ResultDto<HogwartsTestUser> update(HogwartsTestUser hogwartsTestUser);
    public ResultDto<List<HogwartsTestUser>> getByName(HogwartsTestUser hogwartsTestUser);
    public ResultDto delete(Integer userId);
}
