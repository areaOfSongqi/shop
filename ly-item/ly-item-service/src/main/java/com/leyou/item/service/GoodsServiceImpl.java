package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;


    @Autowired
    private CategoryService categoryService;


    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Override
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, String sortBy, Boolean desc, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤

        Example example=new Example(Spu.class);

        Example.Criteria criteria=example.createCriteria();


        //过滤条件  WHERE
        if (StringUtils.isNotBlank(key))
        {
            criteria.andLike("title","%"+key+"%");
        }

        if (saleable!=null)
        {
            criteria.andEqualTo("saleable",saleable);
        }



        //排序
        if (StringUtils.isNotBlank(sortBy))
        {
            //ORDER BY
            example.setOrderByClause(sortBy+(desc?" DESC":" ASC"));
        }

        //查询

        List<Spu> goodsList=spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(goodsList))
        {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //解析商品的分类和品牌名称
        loadCategoryAndBrandName(goodsList);

        PageInfo<Spu> goodsPageInfo=new PageInfo<>(goodsList);

        return new PageResult<>(goodsPageInfo.getTotal(),goodsList);
    }

    private void loadCategoryAndBrandName(List<Spu> goodsList) {

        for (Spu goods:goodsList) {
            //处理每个商品的分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(goods.getCid1(), goods.getCid2(), goods.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            goods.setCname(StringUtils.join(names,"/"));


            //处理每个商品的品牌名称
            Brand brand = brandService.queryBrandById(goods.getBrandId());
            goods.setBname(brand.getName());

        }

    }


    @Transactional
    @Override
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setSaleable(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setValid(false);
        int count = spuMapper.insert(spu);

        if (count!=1)
        {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //新增spuDetail

        SpuDetail spuDetail=spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);

        //新增sku和库存
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());

    }


    private void saveSkuAndStock(Spu spu) {
        int count;
        //定义stock集合，用于批量新增
        List<Stock> stocks=new ArrayList<>();

        List<Sku> skus=spu.getSkus();
        for (Sku sku:skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);

            if (count!=1)
            {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }


            //新增stock
            Stock stock=new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

//            stockMapper.insert(stock);
            stocks.add(stock);
        }

        //批量新增stock
        count = stockMapper.insertList(stocks);
        if (count!=stocks.size())
        {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }


    @Override
    public SpuDetail querySpuDetailById(Long spuId) {
        SpuDetail spuDetail=spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail==null)
        {
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    @Override
    public List<Sku> querySkuById(Long spuId) {
        //查询SKU
        Sku sku=new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus=skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus))
        {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //查询库存
//        for (Sku s : skus) {
//            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
//            if (stock==null)
//            {
//                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
//            }
//            s.setStock(stock.getStock());
//        }
//
//        return skus;

        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockInSku(ids, skus);
        return skus;

    }

    @Transactional
    @Override
    public void updateGoods(Spu spu) {
        if (spu.getId()==null)
        {
            throw new LyException(ExceptionEnum.GOODS_ID_NOT_BE_NULL);
        }
        //1.查询，删除sku
        //spu数据可以修改，但是SKU数据无法修改，因为有可能之前存在的SKU现在已经不存在了，或者以前的sku属性都不存在了
        //因此这里直接删除以前的SKU，然后新增即可。
        Sku sku=new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList))
        {
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }

        //2.修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setCreateTime(null);
        spu.setLastUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count!=1)
        {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        //3.修改spuDetail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count!=1)
        {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        //4.新增sku和stock
        saveSkuAndStock(spu);

        //5.发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());

    }

    @Override
    public Spu querySpuById(Long id) {
        //查spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu==null)
        {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //查sku
        List<Sku> skus = querySkuById(id);
        spu.setSkus(skus);

        //查spuDetail
        SpuDetail spuDetail = querySpuDetailById(id);
        spu.setSpuDetail(spuDetail);

        return spu;
    }


    @Override
    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus))
        {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //查库存
        loadStockInSku(ids, skus);

        return skus;
    }

    private void loadStockInSku(List<Long> ids, List<Sku> skus) {
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)) {
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        //把stock变成一个map:key是sku的id,value是库存数值
        Map<Long, Integer> stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }


    @Transactional
    @Override
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            //减库存
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count!=1)
            {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
