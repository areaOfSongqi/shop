package com.leyou.user.service;

import com.leyou.user.pojo.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    Boolean checkUserData(String data, Integer type);

    void sendCode(String phone);

    void registerUser(User user, String code);

    User queryUserByUsernameAndPassword(String username, String password);
}
