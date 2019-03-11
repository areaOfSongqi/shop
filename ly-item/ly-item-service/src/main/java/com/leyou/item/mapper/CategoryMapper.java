package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * CategoryMapper除了继承通用Mapper以外，要根据id来查找目录信息，还得继承IdListMapper
 */
public interface CategoryMapper extends Mapper<Category>, IdListMapper<Category,Long> {
}
