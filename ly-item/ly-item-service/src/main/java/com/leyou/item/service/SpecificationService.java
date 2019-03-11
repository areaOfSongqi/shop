package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SpecificationService {

    List<SpecGroup> queryGroupByCid(Long cid);

    List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching);

    List<SpecGroup> queryGroupAndParamByCid(Long cid);
}
