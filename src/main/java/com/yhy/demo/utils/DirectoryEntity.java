package com.yhy.demo.utils;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * directory 二维码目录表实体类
 *
 * @author yuhanyi
 * @date 2020/11/16
 */
@Data
@ApiModel("二维码目录表")
@Table(name = "directory")
public class DirectoryEntity {

    /**
     * 逻辑主键ID
     */
    @Id
    @KeySql(useGeneratedKeys = true)
    @ApiModelProperty(hidden = true)
    private Long id;

    /**
     * 目录名
     */
    @ApiModelProperty("目录名")
    private String directoryName;

    /**
     * 父目录Id
     */
    @ApiModelProperty("父目录Id")
    private Long parentId;

    /**
     * 企业编号
     */
    @ApiModelProperty("企业编号")
    private String corpCode;

    /**
     * 逻辑删除符号（0：正常，1：被删除）
     */
    @ApiModelProperty("逻辑删除符号（0：正常，1：被删除")
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;

    /**
     * 创建人id
     */
    @ApiModelProperty("创建人id")
    private Long creatorId;

    /**
     * 消除lombok异味
     * @return
     */
    public Date getCreateTime() {
        return createTime == null ? null : new Date(createTime.getTime());
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime == null ? null : new Date(createTime.getTime());
    }


    public Date getUpdateTime() {
        return updateTime == null ? null : new Date(updateTime.getTime());
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime == null ? null : new Date(updateTime.getTime());
    }


}
