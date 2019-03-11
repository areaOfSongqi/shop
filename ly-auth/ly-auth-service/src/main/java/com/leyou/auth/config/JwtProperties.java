package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String secret; // 密钥

    private String pubKeyPath;// 公钥

    private String priKeyPath;// 私钥

    private int expire;// token过期时间

    private String cookieName;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    //对象一旦实例化，就去读取公钥和私钥
    @PostConstruct
    public void init(){
        try {
            //公钥私钥不存在，就先构建
            File pubPath = new File(pubKeyPath);
            File priPath = new File(priKeyPath);
            if (!pubPath.exists() || !priPath.exists())
            {
                RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
            }


            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey=RsaUtils.getPrivateKey(priKeyPath);

        }catch (Exception e)
        {
            log.error("[公钥私钥生成失败：]"+e);
            e.printStackTrace();
        }
    }

}
