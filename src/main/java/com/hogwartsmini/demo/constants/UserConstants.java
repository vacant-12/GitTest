package com.hogwartsmini.demo.constants;

/**
 * 用户相关常量
 */
public interface UserConstants {


    String md5Hex_sign = "hogwarts-mini";

    /** 登陆token(nginx中默认header无视下划线) */
    String LOGIN_TOKEN = "token";
}