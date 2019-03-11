package com.leyou.item.service;


import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    @Override
    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup=new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> groupList=specGroupMapper.select(specGroup);

        if (CollectionUtils.isEmpty(groupList))
        {
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return groupList;
    }


    @Override
    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam specParam=new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> paramList=specParamMapper.select(specParam);

        if (CollectionUtils.isEmpty(paramList))
        {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }

        return paramList;
    }


    @Override
    public List<SpecGroup> queryGroupAndParamByCid(Long cid) {
        //查group
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        //查当前分类下的所有param
        List<SpecParam> specParams = queryParamList(null, cid, null);

        //先把specParams变成map，key为groupId,分好类
        Map<Long,List<SpecParam>> map=new HashMap<>();
        for (SpecParam param : specParams) {
            if (!map.containsKey(param.getGroupId()))
            {
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }

        for (SpecGroup group : specGroups) {
            group.setParams(map.get(group.getId()));
        }

        return specGroups;
    }
}
