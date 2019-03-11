package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import com.sun.corba.se.spi.ior.ObjectKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;


    public Goods buildGoods(Spu spu)
    {
        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
           //这里已经抛过异常了
        List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());

        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //1.拼接搜索字段
        String all=spu.getSubTitle()+ StringUtils.join(categoryNames," ")+brand.getName();

        //2.价格和所有sku,仅封装id、价格、标题、图片，并获得价格集合
        List<Sku> skus = goodsClient.querySkuById(spu.getId());
        //Set<Long> priceSet = skus.stream().map(Sku::getPrice).collect(Collectors.toSet());
        HashSet<Long> priceSet = new HashSet<>();
        List<Map<String,Object>> newSkus=new ArrayList<>();
        for (Sku sku : skus) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",sku.getId());
            map.put("price",sku.getPrice());
            map.put("title",sku.getTitle());
            //images里可能有大量图片，但我们只需要第一张
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            newSkus.add(map);

            priceSet.add(sku.getPrice());
        }


        //3.商品分类
        //查询规格参数param
        List<SpecParam> paramList = specificationClient.queryParamList(null, spu.getCid3(), true);
        //查询商品详情spuDetail
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());

        //拿到通用规格参数
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //拿到特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });


        //key是规格参数的名字，value是规格参数的值
        Map<String,Object> specs=new HashMap<>();

        for (SpecParam param : paramList) {
            String key=param.getName();
            Object value="";

            if (param.getGeneric())
            {
                value=genericSpec.get(param.getId());

                //由于数值信息存在分段查询，为了方便查询，我们存数值信息的时候，存入选择好的具体分段，而不是具体数值
                if (param.getNumeric())
                {
                    value = chooseSegment(value.toString(), param);
                }
            }else {
                value=specialSpec.get(param.getId());
            }

            specs.put(key,value);
        }


        Goods goods=new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setCreateTime(spu.getCreateTime());


        goods.setAll(all); // 搜索字段，包括标题，分类，品牌，规格等
        goods.setPrice(priceSet);// 所有sku的价格
        goods.setSkus(JsonUtils.toString(newSkus));// 所有sku的信息，冗余的信息可以不要，json形式
        goods.setSpecs(specs);// 分类



        return goods;
    }


    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    public PageResult<Goods> queryGoods(SearchRequest request) {
        //elasticsearch默认从第0页开始
        Integer page = request.getPage()-1;
        Integer size = request.getSize();

        //创建查询构建器
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        //0.结果过滤，只需要显示的数据即可，多余信息不要
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));

        //1.分页
        searchQueryBuilder.withPageable(PageRequest.of(page,size));
        QueryBuilder basicQuery = buildBasicQuery(request);
        //2.过滤
        searchQueryBuilder.withQuery(basicQuery);

        /**
         * 添一步  聚合
         * 1.聚合分类
         * 2.聚合品牌
         */
        String categoryAggName="category_agg";
        searchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));

        String brandAggName="brand_agg";
        searchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));


        //3.查询
        //用聚合 就不能用 repository，要用 template
//        Page<Goods> searchResult = goodsRepository.search(searchQueryBuilder.build());

        AggregatedPage<Goods> searchResult = template.queryForPage(searchQueryBuilder.build(), Goods.class);

        //4.解析结果
        // 分页结果
        long total = searchResult.getTotalElements();
        long totalPage = searchResult.getTotalPages();
        List<Goods> goodsList = searchResult.getContent();

        // 聚合结果
        Aggregations aggregations = searchResult.getAggregations();
        // 分类
        List<Category> categories=parseCategoryAgg(aggregations.get(categoryAggName));
        // 品牌
        List<Brand> brands=parseBrandAgg(aggregations.get(brandAggName));



        // 规格参数
        List<Map<String,Object>> specs=null;

        if (categories!=null && categories.size()==1)
        {
            //在原来搜索的基础上，进行规格参数聚合
            specs=buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }


        return new SearchResult(total,totalPage,goodsList,categories,brands,specs);

    }


    //查询和过滤一起，得用boolQuery
    private QueryBuilder buildBasicQuery(SearchRequest request) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        //过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            //处理key:两种，一种是id，一种是字符串
            if (!key.equals("cid3") && !key.equals("brandId"))
            {
                key="specs."+key+".keyword";
            }
            String value = entry.getValue();
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,value));
        }
        return boolQueryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {

        List<Map<String, Object>> specs=new ArrayList<>();
        //1.根据cid查询需要聚合的规格参数
        List<SpecParam> paramList = specificationClient.queryParamList(null, cid, true);
        //2.在原来搜索的基础上聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);

        for (SpecParam param : paramList) {
            //将规格参数的名字作为term
            String name=param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }

        //3.获取聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //4.解析聚合结果
        Aggregations aggregations = result.getAggregations();

        for (SpecParam param : paramList) {
            String name = param.getName();
            StringTerms stringTerms = aggregations.get(name);

            List<String> options = stringTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

            Map<String,Object> map=new HashMap<>();
            map.put("k",name);
            map.put("options",options);

            specs.add(map);
        }


        return specs;

    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> categoryIds = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(categoryIds);
            return categories;
        } catch (Exception e)
        {
            log.error("分类聚合出错："+e);
            return null;
        }

    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> brandIds = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(brandIds);
            return brands;
        } catch (Exception e)
        {
            log.error("品牌聚合出错："+e);
            return null;
        }

    }

    public void createOrUpdateIndex(Long spuId) {

        Spu spu = goodsClient.querySpuById(spuId);

        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRepository.save(goods);


    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
