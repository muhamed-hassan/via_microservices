package com.practice.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.practice.persistence.entities.EmployeeEntity;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long>, JpaSpecificationExecutor<EmployeeEntity> {

    @Modifying
    @Query(value = "UPDATE employee "
                    + "SET    email = :newEmail "
                    + "WHERE  id = :employeeId",
                    nativeQuery = true)
    int updateEmail(@Param("employeeId") long employeeId, @Param("newEmail") String newEmail);

}
