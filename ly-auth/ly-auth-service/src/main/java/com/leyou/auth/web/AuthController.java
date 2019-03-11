package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录授权
     * @param username
     * @param password
     * @param response
     * @param request
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response,
            HttpServletRequest request
    )
    {
        String token=authService.login(username,password);

        //将token写到cookie中
        CookieUtils.newBuilder(response).httpOnly().request(request).build(jwtProperties.getCookieName(),token);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 检验用户登录情况
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletResponse response,
                                           HttpServletRequest request)
    {
        if (StringUtils.isBlank(token)) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            //重新生成token，刷新存活时间
            String newToken = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            CookieUtils.newBuilder(response).httpOnly().request(request).build(jwtProperties.getCookieName(),token);

            return ResponseEntity.ok(userInfo);
        }catch (Exception e)
        {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

}
