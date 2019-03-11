package com.leyou.page.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface PageService {
    Map<String, Object> loadModel(Long spuId);

    void createHtml(Long spuId);

    void deleteHtml(Long spuId);
}
