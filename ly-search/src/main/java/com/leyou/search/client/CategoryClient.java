package com.leyou.search.client;

import com.leyou.item.api.CategoryApi;
import com.leyou.item.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign用于服务之间的调用
 */

@FeignClient(value = "item-service")
public interface CategoryClient extends CategoryApi {

//    @GetMapping("category/list/ids")
//    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);
}
