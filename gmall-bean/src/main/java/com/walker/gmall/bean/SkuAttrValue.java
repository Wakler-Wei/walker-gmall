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
public class SkuAttrValue  implements Serializable {

    @Id
    @Column
    String id;

    @Column
    String attrId;

    @Column
    String valueId;

    @Column
    String skuId;

}
