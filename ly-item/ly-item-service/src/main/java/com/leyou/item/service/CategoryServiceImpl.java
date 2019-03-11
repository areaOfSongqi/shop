package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> queryCategoryByPid(Long pid) {
        Category category=new Category();
        category.setParentId(pid);
        List<Category> list=categoryMapper.select(category);

        //查询到之后判断是否需要抛异常
        if (CollectionUtils.isEmpty(list))
        {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FIND);
        }

        return list;
    }


    @Override
    public List<Category> queryByIds(List<Long> ids) {
        List<Category> categoryList = categoryMapper.selectByIdList(ids);

        if (CollectionUtils.isEmpty(categoryList))
        {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FIND);
        }

        return categoryList;
    }
}
