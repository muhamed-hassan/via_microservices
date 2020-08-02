package com.practice.services;

import java.util.List;

import com.practice.web.dtos.NewEmployeeDto;
import com.practice.web.dtos.RateAlertDto;
import com.practice.web.dtos.SavedEmployeeDto;

public interface EmployeeService {

    List<SavedEmployeeDto> getEmployees();

    SavedEmployeeDto getEmployeeByFieldCriteria(String fieldCriteria);

    long createEmployee(NewEmployeeDto employeeDto);

    void updateEmployeeEmailById(long id, String email);

    void deleteEmployeeById(long id);

    void registerForScheduledMailAlert(RateAlertDto rateAlertDto);

}
