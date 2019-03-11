package com.leyou.page.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Feign用于服务之间的调用
 */

@FeignClient(value = "item-service")
public interface CategoryClient extends CategoryApi {
}
