package com.encdata.saas.qrcode.generator.impl.directory.service.impl;


import com.yhy.demo.utils.DirectoryTreeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TreeUtil{

    @Autowired
    DirectoryDAO directoryDAO;

    @Override
    public List<DirectoryTreeDTO> queryDirectoryTree() {
        List<DirectoryTreeDTO> resultTree = new ArrayList<DirectoryTreeDTO>();
        List<DirectoryTreeDTO> directoryTreeDTOList = getDirectoryList();
        //递归当前节点下的二维码数量
        for (DirectoryTreeDTO treeNode : directoryTreeDTOList) {
            List<DirectoryTreeDTO> childList = new ArrayList<>();
            getChildList(treeNode, childList, directoryTreeDTOList);
            int childQrcodeNum = childList.stream().mapToInt(i -> i.getQrcodeNum()).sum();
            treeNode.setQrcodeNum(treeNode.getQrcodeNum() + childQrcodeNum);
        }
        //构建目录树
        for (DirectoryTreeDTO treeNode : directoryTreeDTOList) {
            if (treeNode.getParentId() == 0) {
                resultTree.add(findDirectoryChild(treeNode, directoryTreeDTOList));
            }
        }

        return resultTree;
    }

    /**
     * 获取所有的目录
     *
     * @return
     */
    private List<DirectoryTreeDTO> getDirectoryList() {
        //获取企业编号
        String corpCode = SecureUtil.getUser().getCorpId();
        List<DirectoryTreeDTO> directoryTreeDTOS = null;
        try {
            directoryTreeDTOS = directoryDAO.getDirectoryList(corpCode);
        } catch (Exception e) {
            log.error("查询二维码目录数据失败", e);
            throw new RCException("查询二维码目录数据失败");
        }
        return directoryTreeDTOS;
    }

    /**
     * 递归构建目录树
     *
     * @param root      根节点
     * @param treeNodes 所有节点
     * @return
     */
    private DirectoryTreeDTO findDirectoryChild(DirectoryTreeDTO root, List<DirectoryTreeDTO> treeNodes) {
        for (DirectoryTreeDTO it : treeNodes) {
            if (root.getId().equals(it.getParentId())) {
                //防止添加子节点出现空引用
                if (root.getChildren() == null) {
                    root.setChildren(new ArrayList<>());
                }
                root.getChildren().add(findDirectoryChild(it, treeNodes));
            }
        }
        return root;
    }

    /**
     * 递归当前节点下所有子节点
     *
     * @param root
     * @param treeNodes
     * @return
     */
    private void getChildList(DirectoryTreeDTO root, List<DirectoryTreeDTO> childList, List<DirectoryTreeDTO> treeNodes) {

        for (DirectoryTreeDTO it : treeNodes) {
            if (root.getId().equals(it.getParentId())) {
                childList.add(it);
                getChildList(it, childList, treeNodes);
            }
        }
    }

    @Override
    public Boolean addDirectory(Long parentId, String directoryName) {
        DirectoryEntity directoryEntity = new DirectoryEntity();
        directoryEntity.setCorpCode(SecureUtil.getUser().getCorpId());
        directoryEntity.setCreatorId(SecureUtil.getUser().getUserId());
        directoryEntity.setDirectoryName(directoryName);
        directoryEntity.setParentId(parentId);

        if (checkDirectoryName(parentId, directoryName, null)) {
            throw new RCException("目录名已存在");
        }
        try {
            directoryDAO.insertSelective(directoryEntity);
        } catch (Exception e) {
            log.error("新增二维码目录失败", e);
            throw new RCException("新增二维码目录失败");
        }
        return true;
    }

    @Override
    public Boolean modifyDirectory(Long directoryId, String directoryName) {
        DirectoryEntity directory = new DirectoryEntity();
        //判断同级目录下，除自己是否有重名
        try {
            directory = directoryDAO.selectByPrimaryKey(directoryId);
        } catch (Exception e) {
            log.error("查询当前目录失败", e);
            throw new RCException("查询当前目录失败");
        }
        if (checkDirectoryName(directory.getParentId(), directoryName, directoryId)) {
            throw new RCException("目录名已存在");
        }
        DirectoryEntity modifyDirectory = new DirectoryEntity();
        modifyDirectory.setId(directoryId);
        modifyDirectory.setDirectoryName(directoryName);
        try {
            directoryDAO.updateByPrimaryKeySelective(modifyDirectory);
        } catch (Exception e) {
            log.error("修改目录名失败", e);
            throw new RCException("修改目录名失败");
        }

        return true;

    }

    /**
     * 判断同级目录下是否重名
     *
     * @param parentId      父目录id
     * @param directoryName 目录名
     * @return
     */
    private Boolean checkDirectoryName(Long parentId, String directoryName, Long directoryId) {
        //判断同级目录下是否有重名
        Example example = new Example(DirectoryEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("directoryName", directoryName);
        criteria.andEqualTo("parentId", parentId);
        //修改操作时，传入目录id,只判断除自己的重名情况
        if (directoryId != null) {
            criteria.andNotEqualTo("id", directoryId);
        }
        int count = 0;
        try {
            count = directoryDAO.selectCountByExample(example);
        } catch (Exception e) {
            log.error("查询二维码目录失败", e);
            throw new RCException("查询二维码目录失败");
        }
        return count > 0;
    }

    @Override
    public Boolean deleteDirectory(Long directoryId) {
        //递归获取当前目录下所有子目录的id
        List<DirectoryTreeDTO> deleteDirectoryList = new ArrayList<>();

        DirectoryTreeDTO directoryTreeDTO = new DirectoryTreeDTO();
        directoryTreeDTO.setId(directoryId);

        List<DirectoryTreeDTO> directoryList = getDirectoryList();

        getChildList(directoryTreeDTO, deleteDirectoryList, directoryList);
        List<Long> deleteIds = deleteDirectoryList.stream().map(i -> i.getId()).collect(Collectors.toList());
        deleteIds.add(directoryId);

        //判断该目录及其子目录下是否有二维码
        int qrcodeNum = directoryList.stream()
                .filter(i -> deleteIds.contains(i.getId()))
                .mapToInt(i -> i.getQrcodeNum())
                .sum();
        if (qrcodeNum > 0) {
            throw new RCException("目录下已有二维码，删除失败");
        }

        //逻辑删除操作
        Example example = new Example(DirectoryEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", deleteIds);
        DirectoryEntity directoryEntity = new DirectoryEntity();
        directoryEntity.setDeleteFlag(1);
        try {
            directoryDAO.updateByExampleSelective(directoryEntity, example);
        } catch (Exception e) {
            log.error("逻辑删除失败", e);
            throw new RCException("逻辑删除失败");
        }

        return true;
    }

}
