package com.hu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hu.yygh.model.cmn.Dict;
import com.hu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

/**
 * @author suhu
 * @createDate 2022/2/10
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {
    private final BaseMapper<Dict> baseMapper;

    public DictListener(BaseMapper<Dict> baseMapper) {
        this.baseMapper = baseMapper;
    }

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        baseMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
