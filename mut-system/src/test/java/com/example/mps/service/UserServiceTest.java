package com.example.mps.service;

import com.example.mps.pojo.domain.UserDo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import static com.example.mps.constant.UserConstant.PASSWORD_SALT;

@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void save(){
        UserDo user = new UserDo();
        user.setUserName("dogYupi");
        user.setLoginName("12345678");
        user.setAvatarUrl("https://pic.code-nav.cn/user_avatar/1601072287388278786/thumbnail/66mwv6CDAmxwVfNi.jpeg");
        user.setGender(0);
        user.setLoginPwd(DigestUtils.md5DigestAsHex((PASSWORD_SALT + "12345678").getBytes()));
        user.setPhone("123");
        user.setEmail("456");
        userService.save(user);
    }

}