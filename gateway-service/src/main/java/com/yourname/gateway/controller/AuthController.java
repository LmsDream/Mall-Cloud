package com.yourname.gateway.controller;

import com.yourname.gateway.utils.JwtUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password){
        //模拟验证（实际上应该去数据库查询）
        if ("admin".equals(username) && "123".equals(password)){
            return JwtUtils.generateToken(username);
        }
        throw new RuntimeException("用户名或密码错误");
    }

}
