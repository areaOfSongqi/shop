package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;


    @Autowired
    private TemplateEngine templateEngine;



    @Override
    public Map<String, Object> loadModel(Long spuId) {

        Map<String, Object> model=new HashMap<>();
        //1.查spu(同时也查好了skus和detail)
        Spu spu = goodsClient.querySpuById(spuId);
        List<Sku> skus = spu.getSkus();
        SpuDetail detail = spu.getSpuDetail();
        //2.查brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //3.查categories
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //4.查规格参数和规格组
        List<SpecGroup> specs = specificationClient.queryGroupAndParamByCid(spu.getCid3());

        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("detail",detail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specs);
        return model;
    }


    @Override
    public void createHtml(Long spuId) {
        //1.上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));

        //2.输出流
        File file = new File("/Users/songqi/Desktop/乐优商城/upload", spuId + ".html");

        if (file.exists())
        {
            file.delete();
        }


        try (PrintWriter writer = new PrintWriter(file, "UTF-8")){

            templateEngine.process("item",context,writer);
        }catch (Exception e)
        {
            log.error("生成静态页异常:",e);
        }
    }


    @Override
    public void deleteHtml(Long spuId) {

        File file = new File("/Users/songqi/Desktop/乐优商城/upload", spuId + ".html");

        if (file.exists())
        {
            file.delete();
        }

    }
}
