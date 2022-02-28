package com.hu.yygh.user.client;

import com.hu.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author suhu
 * @createDate 2022/2/28
 */
@FeignClient(name = "service-user")
@Component
public interface PatientFeignClient {

    @GetMapping("/api/user/patient/inner/{patientId}")
    Patient getPatient(@PathVariable("patientId") String patientId);
}
