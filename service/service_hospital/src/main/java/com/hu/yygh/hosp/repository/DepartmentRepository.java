package com.hu.yygh.hosp.repository;

import com.hu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DepartmentRepository extends MongoRepository<Department, String> {
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

    List<Department> getDepartmentsByHoscode(String hoscode);

}
