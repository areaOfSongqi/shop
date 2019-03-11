package com.leyou.upload.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface UploadService {

    String uploadImage(MultipartFile file);
}
