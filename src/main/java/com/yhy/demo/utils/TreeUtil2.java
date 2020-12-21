package com.yhy.demo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * 计算当前节点下的所有子节点
     *
     * @param levelChild 每一层的节点
     * @param allChild   所有子节点
     * @param nodesMap   父节点下的所有子节点
     */
    private void getChildList(List<DirectoryTreeDTO> levelChild, List<DirectoryTreeDTO> allChild, Map<Long, List<DirectoryTreeDTO>> nodesMap) {
        List<DirectoryTreeDTO> nodeList = new ArrayList<>();
        for (DirectoryTreeDTO node : levelChild) {
            List<DirectoryTreeDTO> children = Optional.ofNullable(nodesMap.get(node.getId())).orElse(new ArrayList<>());
            if (!children.isEmpty()) {
                nodeList.addAll(children);
                allChild.addAll(children);
            }
        }
        if (!nodeList.isEmpty()) {
            getChildList(nodeList, allChild, nodesMap);
        }
    }

    /**
     * 计算当前节点所在层级
     *
     * @param treeNode  当前节点
     * @param treeNodes 所有节点
     * @return int
     */
    private int countDirLevelToRoot(DirectoryTreeDTO treeNode, List<DirectoryTreeDTO> treeNodes) {
        int level = 0;
        while (treeNode.getParentId() != 0) {
            for (DirectoryTreeDTO node : treeNodes) {
                if (node.getId().equals(treeNode.getParentId())) {
                    level++;
                    treeNode = node;
                }

            }
        }
        return level + 1;
    }


    /**
     * 计算当前节点下的层级（深度）
     *
     * @param levelNodes 每一层的节点
     * @param level      层级
     * @param nodeMap    父节点下的所有子节点
     * @return int
     */
    private int countDirLevelToLeaf(List<DirectoryTreeDTO> levelNodes, int level, Map<Long, List<DirectoryTreeDTO>> nodeMap) {
        List<DirectoryTreeDTO> nodeList = new ArrayList<>();
        for (DirectoryTreeDTO node : levelNodes) {
            List<DirectoryTreeDTO> children = Optional.ofNullable(nodeMap.get(node.getId())).orElse(new ArrayList<>());
            if (!children.isEmpty()) {
                nodeList.addAll(children);
            }
        }
        if (!nodeList.isEmpty()) {
            return countDirLevelToLeaf(nodeList, level + 1, nodeMap);
        }
        return level + 1;
    }


}
