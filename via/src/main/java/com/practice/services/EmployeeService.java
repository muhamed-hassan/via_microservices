package com.practice.services;

import java.util.List;

import com.practice.persistence.entities.EmployeeEntity;
import com.practice.web.dtos.EmployeeDto;

public interface EmployeeService {

    List<EmployeeEntity> getEmployees();

    EmployeeEntity getEmployeeByFieldCriteria(String fieldCriteria);

    long createEmployee(EmployeeDto employeeDto);

    void updateEmployeeEmailById(long id, String email);

    void deleteEmployeeById(long id);

}
