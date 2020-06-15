package com.xqy.es.entity.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 热门活动
 * </p>
 *
 * @author generator
 * @since 2020-05-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class KeyWord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 关键字
     */
    private String keyWord;


}
