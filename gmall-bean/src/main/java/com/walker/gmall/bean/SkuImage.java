package com.walker.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @Author Walker
 * @Date 2019/12/30 18:34
 * @Version 1.0
 */
@Data
public class SkuImage implements Serializable {

    @Id
    @Column
    String id;
    @Column
    String skuId;
    @Column
    String imgName;
    @Column
    String imgUrl;
    @Column
    String spuImgId;
    @Column
    String isDefault;

}
