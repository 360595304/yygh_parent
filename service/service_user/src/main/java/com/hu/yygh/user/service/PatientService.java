package com.hu.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hu.yygh.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {
    List<Patient> findAllByUserId(Long userId);

    Patient getPatientById(Long id);
}
