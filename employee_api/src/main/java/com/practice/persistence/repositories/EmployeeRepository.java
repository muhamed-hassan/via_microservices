package com.practice.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.practice.persistence.entities.Employee;
import com.practice.web.dtos.SavedEmployeeDto;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

//    @Modifying
//    @Query(value = "UPDATE employee "
//                    + "SET    email = :newEmail "
//                    + "WHERE  id = :employeeId",
//                    nativeQuery = true)
//    int updateEmail(@Param("employeeId") long employeeId, @Param("newEmail") String newEmail);

//    Optional<SavedEmployeeDto> findById(int id, Class<SavedEmployeeDto> savedEmployeeDtoClass);

    @Query("SELECT new com.practice.web.dtos.SavedEmployeeDto(e.id, e.name, e.username, e.email, e.phoneNumber, e.age) "
        + "FROM Employee e")
    List<SavedEmployeeDto> findAllSavedEmployees();

}
