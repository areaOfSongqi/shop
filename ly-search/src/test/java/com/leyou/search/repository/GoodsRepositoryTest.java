package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * 为了方便，在测试类中创建索引库
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void testCreateIndex()
    {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData()
    {
        int page=1;
        int rows=100;
        int size=0;

        do {
            //查询spu
            PageResult<Spu> pageResult = goodsClient.querySpuByPage(page, rows, "id", true, true, null);
            List<Spu> spuList = pageResult.getItems();

            if (CollectionUtils.isEmpty(spuList))
            {
                break;
            }

            //构建goods

            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());

            //存入索引库
            goodsRepository.saveAll(goodsList);

            page++;
            size=spuList.size();


        } while (size==100);


    }


}