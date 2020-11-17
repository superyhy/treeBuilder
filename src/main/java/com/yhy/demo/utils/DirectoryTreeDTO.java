package com.yhy.demo.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("二维码目录树结构")
@Data
public class DirectoryTreeDTO {

    /**
     * 目录id
     */
    @ApiModelProperty("目录id")
    private Long id;

    /**
     * 目录名
     */
    @ApiModelProperty("目录名")
    private String directoryName;

    /**
     * 父目录Id
     */
    @ApiModelProperty("父节点Id")
    private Long parentId;

    /**
     * 当前目录下的二维码数
     */
    @ApiModelProperty("当前目录下的二维码数")
    private Integer qrcodeNum;

    /**
     * 子节点
     */
    @ApiModelProperty("子节点，代表子目录")
    private List<DirectoryTreeDTO> children;

}
