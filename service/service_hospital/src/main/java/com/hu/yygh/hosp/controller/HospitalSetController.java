package com.hu.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hu.yygh.common.utils.MD5;
import com.hu.yygh.hosp.service.HospitalSetService;
import com.hu.yygh.model.hosp.HospitalSet;
import com.hu.yygh.common.result.Result;
import com.hu.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * @author suhu
 * @createDate 2022/2/7
 */
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Api(tags = "医院设置管理")
@CrossOrigin
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("获取分页列表")
    @GetMapping("/findAll")
    public Result<List<HospitalSet>> findAllHospitalSet() {
        return Result.ok(hospitalSetService.list());
    }

    @ApiOperation("逻辑删除医院信息")
    @DeleteMapping("/{id}")
    public Result<Object> removeHospitalSet(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if (flag) return Result.ok();
        else return Result.fail();
    }

    @ApiOperation("分页条件查询")
    @PostMapping("/findPageHospSet/{current}/{limit}")
    public Result<Page<HospitalSet>> findPageHospSet(@PathVariable int current,
                                                     @PathVariable int limit,
                                                     @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        Page<HospitalSet> page = new Page<>(current, limit);
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())) {
            queryWrapper.like("hosname", hospitalSetQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())) {
            queryWrapper.eq("hoscode", hospitalSetQueryVo.getHoscode());
        }
        Page<HospitalSet> pageHospSet = hospitalSetService.page(page, queryWrapper);
        return Result.ok(pageHospSet);
    }

    @ApiOperation("添加医院")
    @PostMapping("/saveHospSet")
    public Result<Object> saveHospitalSet(@RequestBody HospitalSet hospitalSet) {
        hospitalSet.setStatus(1);
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(String.valueOf(System.currentTimeMillis() + random.nextInt(100))));
        boolean save = hospitalSetService.save(hospitalSet);
        if (save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("根据id查询医院设置")
    @GetMapping("/getHospSet/{id}")
    public Result<HospitalSet> getHospSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    @ApiOperation("修改医院设置")
    @PostMapping("/updateHospSet")
    public Result<Object> updateHospSet(@RequestBody HospitalSet hospitalSet) {
        boolean success = hospitalSetService.updateById(hospitalSet);
        if (success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/batchRemove")
    public Result<Object> batchRemoveHospSet(@RequestBody List<Long> ids) {
        boolean success = hospitalSetService.removeByIds(ids);
        if (success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("医院锁定/解锁")
    @PutMapping("/lockHospSet/{id}/{status}")
    public Result<Object> lockHospSet(@PathVariable Long id,
                                      @PathVariable Integer status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean success = hospitalSetService.updateById(hospitalSet);
        if (success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("发送签名密钥")
    @PutMapping("/sendKey/{id}")
    public Result<Object> sendKey(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //todo

//        throw new YyghException(ResultCodeEnum.LOGIN_ACL);
        return Result.ok();
    }


}
