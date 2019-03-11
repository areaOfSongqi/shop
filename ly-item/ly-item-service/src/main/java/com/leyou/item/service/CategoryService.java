package com.leyou.item.service;

import com.leyou.item.pojo.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<Category> queryCategoryByPid(Long pid);


    /**
     * 根据传入的一串id来查找目录信息
     * @param ids
     * @return
     */
    List<Category> queryByIds(List<Long> ids);

}
