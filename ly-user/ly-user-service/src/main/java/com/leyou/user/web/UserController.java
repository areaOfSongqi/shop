package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验前端传来的用户注册数据是否可用
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(
            @PathVariable("data") String data,
            @PathVariable("type") Integer type
    ){
        return ResponseEntity.ok(userService.checkUserData(data,type));
    }

    /**
     * 发送短信 user-service调用短信服务
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 注册用户 user前得加上@Valid用于后台校验
     * @param user
     * @param code
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid User user, @RequestParam("code") String code)
    {
        userService.registerUser(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    )
    {
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }


}
