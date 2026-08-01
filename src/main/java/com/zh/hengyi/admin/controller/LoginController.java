package com.zh.hengyi.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1")
public class LoginController {

    @GetMapping("/login")
    public String login(){
        return "你好，世界！";
    }

}
