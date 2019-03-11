package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Table(name = "tb_spec_group")
@Data
public class SpecGroup {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long cid;
    private String name;

    //添加一个参数属性，方便页面显示
    //在`SpecGroup`中添加一个`SpecParam`的集合，保存该组下所有规格参数
    @Transient
    private List<SpecParam> params;
}