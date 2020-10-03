package com.practice.application.employee;

import java.util.List;

import com.practice.domain.employee.Employee;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;

public interface EmployeeService {

    List<SavedEmployeeDto> getEmployees();

    Employee getEmployeeByFieldCriteria(String fieldName, String fieldValue);

    long createEmployee(Employee employee);

    void updateEmployeeEmailById(long id, String email);

    void deleteEmployeeById(long id);

}
