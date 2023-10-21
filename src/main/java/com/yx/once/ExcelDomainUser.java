package com.yx.once;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * Excel数据封装对象
 */
@Data
public class ExcelDomainUser {


    @ExcelProperty("用户昵称")
    private String username;


    @ExcelProperty("用户账号")
    private String userAccount;


    @ExcelProperty("用户头像")
    private String avatarUrl;


    @ExcelProperty("性别")
    private Integer gender;


    @ExcelProperty("密码")
    private String userPassword;

    @ExcelProperty("电话")
    private String phone;


    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("星球编号")
    private String planetCode;


    @ExcelProperty("用户标签")
    private String tags;

    @ExcelProperty("个人介绍")
    private String profile;
}
