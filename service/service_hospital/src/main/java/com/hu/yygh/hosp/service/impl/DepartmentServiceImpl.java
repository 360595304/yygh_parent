package com.hu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hu.yygh.hosp.repository.DepartmentRepository;
import com.hu.yygh.hosp.service.DepartmentService;
import com.hu.yygh.model.hosp.Department;
import com.hu.yygh.vo.hosp.DepartmentQueryVo;
import com.hu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author suhu
 * @createDate 2022/2/13
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> paramMap) {
        String jsonString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);
        Department departmentExist = departmentRepository
                .getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        if (departmentExist != null) {
            department.setId(departmentExist.getId());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        } else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);
        Example<Department> example = Example.of(department, matcher);
        return departmentRepository.findAll(example, pageable);
    }

    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            departmentRepository.deleteById(department.getId());
        }
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        List<DepartmentVo> result = new ArrayList<>();
        // 根据hoscode查出所有的Department
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentQuery);
        List<Department> departmentList = departmentRepository.findAll(example);
        // 按科室大类分类
        Map<String, List<Department>> departmentMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        // 将数据转换成List<DepartmentVo>格式
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            String bigCode = entry.getKey();
            List<Department> list = entry.getValue();
            DepartmentVo v1 = new DepartmentVo();
            v1.setDepcode(bigCode);
            v1.setDepname(list.get(0).getDepname());
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department : list) {
                DepartmentVo v2 = new DepartmentVo();
                v2.setDepname(department.getDepname());
                v2.setDepcode(department.getDepcode());
                children.add(v2);
            }
            v1.setChildren(children);
            result.add(v1);
        }

        return result;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.findDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            return department.getDepname();
        }
        return null;
    }
}
