package com.walker.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author Walker
 * @Date 2020/1/8 15:07
 * @Version 1.0
 */
@Data
public class CartInfo implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    String id;
    @Column
    String userId;
    @Column
    String skuId;
    @Column
    BigDecimal cartPrice;
    @Column
    Integer skuNum;
    @Column
    String imgUrl;
    @Column
    String skuName;
    @Column
    String isChecked="1";

    // 实时价格
    @Transient
    BigDecimal skuPrice;

}
