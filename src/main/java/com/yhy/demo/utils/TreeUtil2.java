package com.yhy.demo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreeUtil2 {

    /**
     * stream流-构建目录树
     *
     * @param directoryList
     * @return
     */
    private List<DirectoryTreeDTO> buildDirectoryTree(List<DirectoryTreeDTO> directoryList) {
        //以父节点id为key的哈希表
        Map<Long, List<DirectoryTreeDTO>> directoryByParentIdMap = directoryList.stream()
                .collect(Collectors.groupingBy(DirectoryTreeDTO::getParentId));
        //从哈希表中获取所有节点的子节点
        directoryList.forEach(DirectoryTreeDTO -> DirectoryTreeDTO
                .setChildren(directoryByParentIdMap.getOrDefault(DirectoryTreeDTO.getId(), new ArrayList<>())));
        //返回根节点
        return directoryList.stream()
                .filter(i -> i.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
