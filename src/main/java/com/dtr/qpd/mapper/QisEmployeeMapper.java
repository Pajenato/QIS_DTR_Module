package com.dtr.qpd.mapper;

import com.dtr.qpd.dto.QisEmployeeDto;
import com.dtr.qpd.entity.QisEmployee;

public class QisEmployeeMapper {
    public static QisEmployeeDto maptoEmployeeDto(QisEmployee employee) {
        return new QisEmployeeDto(
            employee.getId(),
            employee.getName()
        );
    }   
    
    public static QisEmployee maptoEmployee(QisEmployeeDto employeeDto) {
        return new QisEmployee(
            employeeDto.getId(),
            employeeDto.getName()
        );
    }
}
