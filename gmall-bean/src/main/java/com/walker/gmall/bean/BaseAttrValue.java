package com.walker.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @Author Walker
 * @Date 2019/12/27 18:52
 * @Version 1.0
 */
@Data
public class BaseAttrValue implements Serializable {

    @Id
    @Column
    private String id;
    @Column
    private String valueName;
    @Column
    private String attrId;

    // 业务需要的字段
    @Transient
    private String urlParam;

}
