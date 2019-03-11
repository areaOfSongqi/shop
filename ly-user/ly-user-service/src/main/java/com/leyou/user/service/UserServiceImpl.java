package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX="user:verifyCode:phone:";



    @Override
    public Boolean checkUserData(String data, Integer type) {
        User record = new User();
        //要校验的数据类型：1，用户名；2，手机；
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }

        return userMapper.selectCount(record)==0;
    }

    @Override
    public void sendCode(String phone) {
        String key=KEY_PREFIX+phone;
        //随机生成验证码
        String code= NumberUtils.generateCode(6);

        Map<String,String> msg=new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        //发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verifyCode",msg);

        //保存验证码到redis
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }


    @Override
    public void registerUser(User user, String code) {
        //校验验证码
        //先从redis里取出正确验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!code.equals(cacheCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        user.setCreated(new Date());

        userMapper.insert(user);
    }

    @Override
    public User queryUserByUsernameAndPassword(String username, String password) {
        User record = new User();
        //根据用户名查询
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        if (user==null)
        {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        if (!StringUtils.equals(user.getPassword(),CodecUtils.md5Hex(password,user.getSalt()))) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        return user;
    }
}
