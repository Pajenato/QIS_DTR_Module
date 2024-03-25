package com.dtr.qpd.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.dtr.qpd.dto.QisEmployeeDto;

public interface QisServiceDtr {

    ResponseEntity<String> analyzeAttendanceAndGenerateCsv(MultipartFile file);

    QisEmployeeDto addEmployee(QisEmployeeDto employeeDto);

    QisEmployeeDto getEmployeeById(Long employeeId);

    List<QisEmployeeDto> getAllEmployees();

    QisEmployeeDto updateEmployee(Long id, QisEmployeeDto updatedEmployee);

    void deleteEmployee(Long employeeId);
}
