package com.walker.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @Author Walker
 * @Date 2019/12/27 18:52
 * @Version 1.0
 */
@Data
public class BaseAttrInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY )  //主键自增
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;

    @Transient  // 表示数据库中没有的字段，但是在业务中需要！
    private List<BaseAttrValue> attrValueList;

}
