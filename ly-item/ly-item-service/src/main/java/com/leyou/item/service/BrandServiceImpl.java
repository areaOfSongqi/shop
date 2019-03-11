package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandMapper brandMapper;


    @Override
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤

        Example example=new Example(Brand.class);

        if (StringUtils.isNotBlank(key))
        {
            //过滤条件  WHERE
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy))
        {
            //ORDER BY
            String orderClause=sortBy+(desc?" DESC":" ASC");
            example.setOrderByClause(orderClause);
        }

        //查询

        List<Brand> brandList=brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brandList))
        {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        PageInfo<Brand> brandPageInfo=new PageInfo<>(brandList);

        return new PageResult<>(brandPageInfo.getTotal(),brandList);
    }


    @Transactional
    @Override
    public void saveBrand(Brand brand, List<Long> cids) {
        int count=brandMapper.insert(brand);
        if (count!=1)
        {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        //新增中间表
        for (Long cid:cids) {
            count=brandMapper.insertCategoryBrand(cid,brand.getId());
            if (count!=1)
            {
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    @Override
    public Brand queryBrandById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);

        if (brand==null)
        {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return brand;
    }


    /**
     * 根据cid查询品牌，由于brand和category两表通过中间表关联，所以得自己写sql
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brandList = brandMapper.queryByCategoryId(cid);

        if (CollectionUtils.isEmpty(brandList))
        {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return brandList;

    }

    @Override
    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);

        if (CollectionUtils.isEmpty(brands))
        {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return brands;
    }
}
