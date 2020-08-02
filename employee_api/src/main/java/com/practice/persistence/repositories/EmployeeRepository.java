package com.practice.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.practice.persistence.entities.Employee;
import com.practice.web.dtos.SavedEmployeeDto;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Query("SELECT new com.practice.web.dtos.SavedEmployeeDto(e.id, e.name, e.username, e.email, e.phoneNumber, e.age) "
        + "FROM Employee e")
    List<SavedEmployeeDto> findAllSavedEmployees();

}
