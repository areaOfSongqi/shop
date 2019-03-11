package com.leyou.cart.interceptor;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 因为很多接口都需要进行登录，我们直接编写SpringMVC拦截器，进行统一登录校验。
 * 同时，我们还要把解析得到的用户信息保存起来，以便后续的接口可以使用。
 */

@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProperties;

    //使用了`ThreadLocal`来存储查询到的用户信息，线程内共享，因此请求到达`Controller`后可以共享User
    private static final ThreadLocal<UserInfo> threadLocal=new ThreadLocal<>();

    public UserInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties=jwtProperties;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        //1.获取cookie
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
//            //通过request传递user(不推荐)
//            request.setAttribute("user",user);

            //用ThreadLocal存
            threadLocal.set(user);

            return true;
        } catch (Exception e) {
            log.error("[购物车服务] 解析用户身份失败: "+e);
            return false;
        }

    }


    public static UserInfo getUser()
    {
        return threadLocal.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //用完一定要删除
        threadLocal.remove();
    }
}

