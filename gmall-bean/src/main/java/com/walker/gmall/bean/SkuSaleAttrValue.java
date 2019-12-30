package com.walker.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @Author Walker
 * @Date 2019/12/30 18:35
 * @Version 1.0
 */
@Data
public class SkuSaleAttrValue implements Serializable {

    @Id
    @Column
    String id;

    @Column
    String skuId;

    @Column
    String saleAttrId;

    @Column
    String saleAttrValueId;

    @Column
    String saleAttrName;

    @Column
    String saleAttrValueName;

}
