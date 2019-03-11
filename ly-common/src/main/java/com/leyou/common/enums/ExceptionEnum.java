package com.leyou.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

/**
 * 自定义异常的枚举
 */

public enum ExceptionEnum {

    BRAND_NOT_FOUND(404,"品牌没查到"),
    GOODS_NOT_FOUND(404,"商品没查到"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情没查到"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU没查到"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存没查到"),
    CATEGORY_NOT_FIND(404,"商品分类没查到"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    INVALID_FILE_TYPE(400,"文件类型不匹配"),
    UPLOAD_FILE_ERROR(500,"上传文件失败"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数不存在"),
    GOODS_SAVE_ERROR(500,"保存商品失败"),
    GOODS_UPDATE_ERROR(500,"修改商品失败"),
    GOODS_ID_NOT_BE_NULL(400,"商品ID不能为空"),
    INVALID_USER_DATA_TYPE(400,"用户数据类型无效"),
    INVALID_VERIFY_CODE(400,"验证码无效"),
    INVALID_USERNAME_PASSWORD(400,"用户名或密码无效"),
    CREATE_TOKEN_ERROR(500,"用户凭证生成失败"),
    UNAUTHORIZED(403,"用户未授权"),
    CART_NOT_FOUND(404,"购物车为空"),
    CREATE_ORDER_ERROR(500,"创建订单失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在")
    ;
    private int code;
    private String msg;

}
