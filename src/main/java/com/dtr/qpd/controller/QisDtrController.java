package com.dtr.qpd.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.dtr.qpd.dto.QisEmployeeDto;
import com.dtr.qpd.service.QisServiceDtr;

@RestController
@RequestMapping("/dtr")
@CrossOrigin("*")
public class QisDtrController {
    private final QisServiceDtr qisServiceDtr;

    public QisDtrController(QisServiceDtr qisServiceDtr) {
        this.qisServiceDtr = qisServiceDtr;
    }

    //Build analyzeDtr csv file
    @PostMapping("/analyze")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        
        try {
            ResponseEntity<String> response = qisServiceDtr.analyzeAttendanceAndGenerateCsv(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_analysis.csv");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok().headers(headers).body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to analyze attendance");
        }
    }


    //Adding a new employee using RESTFUL API

    @PostMapping("/addEmp")
    public ResponseEntity<?> createEmployee(@RequestBody QisEmployeeDto employeeDto) {
        if (employeeDto == null || employeeDto.getName() == null || employeeDto.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Employee name is required");
        }
        try {
            QisEmployeeDto savedEmployee = qisServiceDtr.addEmployee(employeeDto);
            return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add employee");
        }
    }
    
    // get a specific employee ID using RESTFUL API

    @GetMapping("/getEmp/{id}")
    public ResponseEntity<QisEmployeeDto> getEmployeeById(@PathVariable("id") Long employeeId) {
        QisEmployeeDto employeeDto = qisServiceDtr.getEmployeeById(employeeId);
        return ResponseEntity.ok(employeeDto);
    }

    // get all employee ID using RESTFUL API

    @GetMapping("/getAllEmp")
    public ResponseEntity<List<QisEmployeeDto>> getAllEmployees() {
        List<QisEmployeeDto> employees = qisServiceDtr.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // update the details of Employee Id using RESTFUL API

    @PutMapping("/updateEmp/{id}")
    public ResponseEntity<QisEmployeeDto> updateEmployee(
            @PathVariable("id") Long employeeId,
            @RequestBody QisEmployeeDto updatedEmployee) {
        // Perform input validation manually
        if (updatedEmployee == null || updatedEmployee.getName() == null || updatedEmployee.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Continue with the business logic if input is valid
        QisEmployeeDto employeeDto = qisServiceDtr.updateEmployee(employeeId, updatedEmployee);
        return ResponseEntity.ok(employeeDto);
    }

    //Delete the employee by getting the id using RESTFUL API 

    @DeleteMapping("/deleteEmp/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable("id") Long employeeId) {
        if (employeeId == null) {
            return ResponseEntity.badRequest().body("Employee ID cannot be null");
        }
        qisServiceDtr.deleteEmployee(employeeId);
        return ResponseEntity.ok("Employee deleted Successfully!, " + "EmployeeId: " + employeeId);
    }
}
