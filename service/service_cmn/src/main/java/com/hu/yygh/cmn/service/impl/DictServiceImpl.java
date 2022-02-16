package com.hu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hu.yygh.cmn.listener.DictListener;
import com.hu.yygh.cmn.mapper.DictMapper;
import com.hu.yygh.cmn.service.DictService;
import com.hu.yygh.model.cmn.Dict;
import com.hu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        List<Dict> dictList = baseMapper.selectList(queryWrapper);
        for (Dict dict : dictList) {
            dict.setHasChildren(this.hasChildren(dict.getId()));
        }
        return dictList;
    }

    @Override
    public void exportDictData(HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = "dict";
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        List<Dict> dictList = baseMapper.selectList(null);
        List<DictEeVo> dictVoList = new ArrayList<>();
        for (Dict dict : dictList) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict, dictEeVo);
            dictVoList.add(dictEeVo);
        }
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDictName(String dictCode, String value) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isEmpty(dictCode)) {
            queryWrapper.eq("value", value);
        } else {
            Dict parent = this.getDictByDictCode(dictCode);
            Long parentId = parent.getId();
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", parentId)
                    .eq("value", value);
        }
        return baseMapper.selectOne(queryWrapper).getName();

    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict parent = this.getDictByDictCode(dictCode);
        if (parent != null) {
            Long parentId = parent.getId();
            return this.findChildData(parentId);
        }
        return null;
    }

    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code", dictCode);
        return baseMapper.selectOne(queryWrapper);
    }

    // 判断id下面是否有子结点
    private boolean hasChildren(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        int count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }
}
