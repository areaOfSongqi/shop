package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@EnableConfigurationProperties(UploadProperties.class)
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    //private static final List<String> ALLOW_TYPES= Arrays.asList("image/jpeg","image/png","image/bmp");

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties uploadProperties;


    @Override
    public String uploadImage(MultipartFile file) {

        try {
            //校验文件类型
            String contentType=file.getContentType();
            if (!uploadProperties.getAllowTypes().contains(contentType))
            {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件内容
            BufferedImage image=ImageIO.read(file.getInputStream());
            if (image==null)
            {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

//            //准备目标路径
//            File dest=new File("/Users/songqi/Desktop/乐优商城/upload/",file.getOriginalFilename());
//            file.transferTo(dest);
            /**
             * 上传到FastDFS
             */

            String extension= StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath=storageClient.uploadImageAndCrtThumbImage(file.getInputStream(),file.getSize(),extension,null);



            return uploadProperties.getBaseUrl()+storePath.getFullPath();

        }catch (IOException e)
        {
            log.error("文件上传失败",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
