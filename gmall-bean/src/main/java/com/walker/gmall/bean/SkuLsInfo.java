package com.walker.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuLsInfo implements Serializable {
    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;

    // 默认的热度排名
    Long hotScore=0L;

    // 平台属性值Id
    List<SkuLsAttrValue> skuAttrValueList;

}
