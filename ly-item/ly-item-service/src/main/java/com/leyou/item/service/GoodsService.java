package com.leyou.item.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GoodsService {
    PageResult<Spu> querySpuByPage(Integer page, Integer rows, String sortBy, Boolean desc, Boolean saleable, String key);

    void saveGoods(Spu spu);

    SpuDetail querySpuDetailById(Long spuId);

    List<Sku> querySkuById(Long spuId);

    void updateGoods(Spu spu);

    Spu querySpuById(Long id);

    List<Sku> querySkuByIds(List<Long> ids);

    void decreaseStock(List<CartDTO> carts);
}
