package com.practice.interfaces.rest.controllers;

import java.net.HttpURLConnection;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.practice.application.employee.EmployeeService;
import com.practice.domain.employee.Employee;
import com.practice.interfaces.rest.assemblers.DtoAssembler;
import com.practice.interfaces.rest.assemblers.EntityAssembler;
import com.practice.interfaces.rest.dtos.EmailDto;
import com.practice.interfaces.rest.dtos.NewEmployeeDto;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;
import com.practice.interfaces.rest.validators.FieldCriteriaRule;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Employee Management API")
@RestController
@RequestMapping(value = "v1/employees")
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    private final EntityAssembler entityAssembler;

    private final DtoAssembler dtoAssembler;

    public EmployeeController(EmployeeService employeeService,
                                EntityAssembler entityAssembler,
                                DtoAssembler dtoAssembler) {
        this.employeeService = employeeService;
        this.entityAssembler = entityAssembler;
        this.dtoAssembler = dtoAssembler;
    }

    @ApiOperation(value = "View a list of available employees")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded to retrieve a list of employees", response = List.class),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to retrieve a list of employees")
    })
    @GetMapping
    public List<SavedEmployeeDto> getEmployees() {
        return employeeService.getEmployees();
    }

    @ApiOperation(value = "Retrieve employee by field criteria")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded to retrieve an employee by field criteria", response = SavedEmployeeDto.class),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Employee not found"),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_IMPLEMENTED, message = "Not supported field criteria is used"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid field criteria format")
    })
    @GetMapping("criteria/{fieldCriteria}")
    public SavedEmployeeDto getEmployeeByFieldCriteria(@PathVariable @FieldCriteriaRule String fieldCriteria) {
        return dtoAssembler.toDto(employeeService.getEmployeeByFieldCriteria(fieldCriteria), SavedEmployeeDto.class);
    }

    @ApiOperation(value = "Create a new employee")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Succeeded to create a new employee"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Employee payload contains invalid value"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to create a new employee")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createEmployee(@RequestBody @Valid NewEmployeeDto newEmployeeDto) {
        return ResponseEntity.created(ServletUriComponentsBuilder
                                        .fromCurrentRequest()
                                        .path("/{id}")
                                        .build(employeeService.createEmployee(entityAssembler.toEntity(newEmployeeDto, Employee.class))))
                                .build();
    }

    @ApiOperation(value = "Update employee's email by id")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Succeeded to update employee's email"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Failed to update employee's email"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to update employee's email")
    })
    @PatchMapping("{id}")
    public ResponseEntity<Void> updateEmployeeEmailById(@PathVariable long id, @RequestBody @Valid EmailDto emailDto) {
        employeeService.updateEmployeeEmailById(id, emailDto.getEmail());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Delete employee by id")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Succeeded to delete employee by id"),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Requested employee for delete doesn't exist"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to delete employee by id")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteEmployeeById(@PathVariable long id) {
        employeeService.deleteEmployeeById(id);
        return ResponseEntity.noContent().build();
    }

}
