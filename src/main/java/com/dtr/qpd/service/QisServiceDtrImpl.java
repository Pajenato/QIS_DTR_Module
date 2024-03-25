package com.dtr.qpd.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
//import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dtr.qpd.dto.QisEmployeeDto;
import com.dtr.qpd.entity.QisEmployee;
import com.dtr.qpd.exception.QisResourceNotFoundException;
import com.dtr.qpd.mapper.QisEmployeeMapper;
import com.dtr.qpd.repository.QisEmployeeRepository;

@Service
public class QisServiceDtrImpl implements QisServiceDtr {
    @Autowired
    private QisEmployeeRepository qisEmployeeRepository;

    public ResponseEntity<String> analyzeAttendanceAndGenerateCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            // Map to track modes and their corresponding dates with times for each employee
            Map<Long, Map<Integer, Map<String, Set<String>>>> employeeModeDateTime = new HashMap<>();
            // Track employee names for easier reference
            Map<Long, String> employeeNames = new HashMap<>();

            for (CSVRecord csvRecord : CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
                long employeeId = Long.parseLong(csvRecord.get("EnNo"));
                int mode = Integer.parseInt(csvRecord.get("Mode"));
                String date = csvRecord.get("Date");
                String time = csvRecord.get("Time");

                QisEmployee employee = qisEmployeeRepository.findById(employeeId).orElse(null);
                if (employee != null) {
                    employeeNames.putIfAbsent(employeeId, employee.getName());
                    employeeModeDateTime
                            .computeIfAbsent(employeeId, k -> new HashMap<>())
                            .computeIfAbsent(mode, k -> new HashMap<>())
                            .computeIfAbsent(date, k -> new HashSet<>())
                            .add(time);
                }
            }

            // Prepare to generate CSV output
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Employee Name", "Days Present", "Remarks"));

            // Analyze attendance
            for (Map.Entry<Long, Map<Integer, Map<String, Set<String>>>> entry : employeeModeDateTime.entrySet()) {
                long employeeId = entry.getKey();
                Map<Integer, Map<String, Set<String>>> modesDateTime = entry.getValue();

                // Calculate days present based on mode-date-time matching
                int daysPresent = calculateDaysPresent(modesDateTime);
                String name = employeeNames.get(employeeId);

                // Prepare remarks for incomplete attendance
                String remarks = prepareIncompleteAttendanceRemarks(modesDateTime);

                csvPrinter.printRecord(name, daysPresent, remarks);
            }

            csvPrinter.close();
            return ResponseEntity.ok().body(writer.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to analyze attendance and generate CSV");
        }
    }

    private int calculateDaysPresent(Map<Integer, Map<String, Set<String>>> modesDateTime) {
        Set<String> datesWithCompleteAttendance = new HashSet<>();
        if (modesDateTime.containsKey(0)) {
            Map<String, Set<String>> datesTimesForMode0 = modesDateTime.get(0);
            if (modesDateTime.containsKey(1)) {
                datesWithCompleteAttendance.addAll(intersection(datesTimesForMode0, modesDateTime.get(1)));
            }
            if (modesDateTime.containsKey(2)) {
                datesWithCompleteAttendance.addAll(intersection(datesTimesForMode0, modesDateTime.get(2)));
            }
        }
        return datesWithCompleteAttendance.size();
    }

    private String prepareIncompleteAttendanceRemarks(Map<Integer, Map<String, Set<String>>> modesDateTime) {
        StringBuilder remarksBuilder = new StringBuilder();
        for (Integer mode : Arrays.asList(0, 1, 2)) {
            if (!modesDateTime.containsKey(mode)) {
                remarksBuilder.append("Missing mode ").append(mode).append(" on ");
                // Get the first date with missing mode
                String date = getFirstDateWithMissingMode(modesDateTime, mode);
                if (date != null) {
                    Set<String> times = modesDateTime.entrySet().stream()
                            .filter(entry -> entry.getKey() != mode && entry.getValue().containsKey(date))
                            .flatMap(entry -> entry.getValue().get(date).stream())
                            .collect(Collectors.toSet());
    
                    for (String time : times) {
                        remarksBuilder.append(date).append(", ").append(time).append(".");
                    }
                }
                remarksBuilder.append(" ");
            }
        }
        return remarksBuilder.toString();
    }
    
    // Helper method to get the first date with missing mode
    private String getFirstDateWithMissingMode(Map<Integer, Map<String, Set<String>>> modesDateTime, int missingMode) {
        for (Map.Entry<Integer, Map<String, Set<String>>> entry : modesDateTime.entrySet()) {
            int mode = entry.getKey();
            if (mode != missingMode) {
                for (String date : entry.getValue().keySet()) {
                    if (!modesDateTime.containsKey(missingMode) || !modesDateTime.get(missingMode).containsKey(date)) {
                        return date;
                    }
                }
            }
        }
        return null;
    }
    
    
    
    // Helper method to find the intersection of dates and times for two modes
    private Set<String> intersection(Map<String, Set<String>> mode1, Map<String, Set<String>> mode2) {
        Set<String> result = new HashSet<>(mode1.keySet());
        result.retainAll(mode2.keySet());
        return result;
    }




    @Override
    public QisEmployeeDto addEmployee(QisEmployeeDto employeeDto) {  
        QisEmployee employee = QisEmployeeMapper.maptoEmployee(employeeDto);
        @SuppressWarnings("null")
        QisEmployee savedEmployee = qisEmployeeRepository.save(employee);
        return QisEmployeeMapper.maptoEmployeeDto(savedEmployee);
    }

    @Override
    public QisEmployeeDto getEmployeeById(Long employeeId) {
        @SuppressWarnings("null")
        QisEmployee employee = qisEmployeeRepository.findById(employeeId).orElseThrow(()-> new QisResourceNotFoundException("Employee is note exists with given id: "+ employeeId));
        return QisEmployeeMapper.maptoEmployeeDto(employee);
    }


    @Override
    public List<QisEmployeeDto> getAllEmployees() {
        List<QisEmployee> employees = qisEmployeeRepository.findAll();
        return employees.stream().map((employee)
        ->QisEmployeeMapper.maptoEmployeeDto(employee))
        .collect(Collectors.toList());
    }


    @Override
    public QisEmployeeDto updateEmployee(Long employeeId, QisEmployeeDto updatedEmployee) {
        @SuppressWarnings("null")
        QisEmployee employee = qisEmployeeRepository.findById(employeeId).orElseThrow(
            () -> new QisResourceNotFoundException("Employee is not exists with given id:" + employeeId)
        );
        employee.setId(updatedEmployee.getId());
        employee.setName(updatedEmployee.getName());
        
        QisEmployee updatedEmployeeObj = qisEmployeeRepository.save(employee);

        return QisEmployeeMapper.maptoEmployeeDto(updatedEmployeeObj);
    }


    @SuppressWarnings("null")
    @Override
    public void deleteEmployee(Long employeeId) {
        @SuppressWarnings("unused")
        QisEmployee employee = qisEmployeeRepository.findById(employeeId).orElseThrow(
            () -> new QisResourceNotFoundException("Employee is not exists with given id:" + employeeId)
        );
        qisEmployeeRepository.deleteById(employeeId);
    }
}


