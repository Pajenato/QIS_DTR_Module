package com.dtr.qpd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dtr.qpd.entity.QisEmployee;

@Repository
public interface QisEmployeeRepository extends JpaRepository<QisEmployee,Long>{

}
