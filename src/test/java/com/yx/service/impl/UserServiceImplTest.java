package com.yx.service.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.internal.JsonFormatter;
import com.yx.constant.UserConstant;
import com.yx.controller.UserController;
import com.yx.domain.User;
import com.yx.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserServiceImplTest {

    @Resource
    UserService userService;
    @Resource
    UserController userController;
    @Resource
    HttpServletRequest servlet;
    @Resource
    RedisTemplate redisTemplate;
    @Test
    public void testRegister(){
    }

    @Test
    public void testLogin(){
        User yxyx = userService.userLogin("yxyx", "12345678", servlet);
        System.out.println(yxyx);
    }

    @Test
    void searchUsersByTags() {
        List<String> tagList = new ArrayList<>();
        tagList.add("大一");
        tagList.add("男");
        tagList.add("Java");
        List<User> users = userService.searchUsersByTags(tagList);
        System.out.println(users);
    }
    @Test
    void searchUsersByTags2() {
        String tagJson ="['男','大一']";
        Gson gson = new Gson();
        Set<String> tagSet = gson.fromJson(tagJson, new TypeToken<Set<String>>() {
        }.getType());
        System.out.println(tagSet);
    }
    @Test
    void searchUsersByTags3() {
        User user = new User();
        user.setUsername("user1002");
        user.setUserAccount("user1002");
        user.setUserPassword("12345678");
        user.setAvatarUrl("https://profile-avatar.csdnimg.cn/60ca7e001d474c74b74f286669dfa183_weixin_43518544.jpg!1");
        user.setUserRole(0);
        user.setPlanetCode("1002");
        user.setEmail("123@qq.com");
        user.setGender(0);
        user.setTags("['大一','男','Java']");
        boolean save = userService.save(user);
        System.out.println(save);
    }

    @Test
    void testRedis(){
        redisTemplate.opsForValue().set("k1:a:a:a","西游记");
        System.out.println(redisTemplate.opsForValue().get("k1"));
    }

    @Test
    public void test(){
        User user = new User();
        user.setId(1l);
        user.setUsername("user1002");
        user.setUserAccount("user1002");
        user.setUserPassword("12345678");
        user.setAvatarUrl("https://profile-avatar.csdnimg.cn/60ca7e001d474c74b74f286669dfa183_weixin_43518544.jpg!1");
        user.setPlanetCode("1002");
        user.setEmail("123@qq.com");
        user.setGender(0);
        user.setProfile("我叫xxx,来自绿城南宁.XXXX年X月份以专业考试得分第一的好成绩毕业于广西大学的xx专业.毕业之前,我曾在xx公司实习过,xx公司和贵公司是同类行业.\n" +
                "本人性格开朗,善于微笑，长于交际,会简单日语及芭蕾舞.我相信,这一切将成为我工作最大的财富.我在很久就注意到贵公司,贵公司无疑是**行业中的姣姣者(将你所了解的公司荣誉或成果填上).同时我又了解到,这又是一支年轻而又富有活力的队伍.本人非常渴望能够在为其中的一员.\n" +
                "如果有幸获聘,本人将以为公司创造最大利益为自己最大的利益,不讲价钱.真诚做好每一件事,和同事们团结奋斗.勤奋工作,加强学习,不断进步!谢谢!");
        user.setTags("['大一','男','Java']");
        Gson gson = new Gson();
        System.out.println(user);
        String s = gson.toJson(user);
        System.out.println(s);

        redisTemplate.opsForValue().set("userString",user);
        redisTemplate.opsForHash().put("userHashmap","id",user.getId());
        redisTemplate.opsForHash().put("userHashmap","userName",user.getUsername());
        redisTemplate.opsForHash().put("userHashmap","userAccount",user.getUserAccount());
        redisTemplate.opsForHash().put("userHashmap","userPassword",user.getUserPassword());
        redisTemplate.opsForHash().put("userHashmap","avatarUrl",user.getAvatarUrl());
        redisTemplate.opsForHash().put("userHashmap","planetCode",user.getPlanetCode());
        redisTemplate.opsForHash().put("userHashmap","email",user.getEmail());
        redisTemplate.opsForHash().put("userHashmap","gender",user.getGender());
        redisTemplate.opsForHash().put("userHashmap","tags",user.getTags());
        redisTemplate.opsForHash().put("userHashmap","profile",user.getProfile());

    }

    @Test
    public void test1(){
        User user= (User)redisTemplate.opsForValue().get("userString");
        System.out.println(user);
       Long id = (Long)redisTemplate.opsForHash().get("userHashmap", "id");
        System.out.println(id);
    }
}