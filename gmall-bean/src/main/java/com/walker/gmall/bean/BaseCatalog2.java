package com.walker.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @Author Walker
 * @Date 2019/12/27 18:50
 * @Version 1.0
 */
@Data
public class BaseCatalog2 implements Serializable {

    @Id
    @Column
    private String id;
    @Column
    private String name;
    @Column
    private String catalog1Id;

}
