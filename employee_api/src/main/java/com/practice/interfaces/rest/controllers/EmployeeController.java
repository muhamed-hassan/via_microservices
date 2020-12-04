package com.practice.interfaces.rest.controllers;

import java.net.HttpURLConnection;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
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
import com.practice.domain.ratealert.RateAlert;
import com.practice.interfaces.rest.assemblers.DtoAssembler;
import com.practice.interfaces.rest.assemblers.EntityAssembler;
import com.practice.interfaces.rest.dtos.EmailDto;
import com.practice.interfaces.rest.dtos.NewEmployeeDto;
import com.practice.interfaces.rest.dtos.RateAlertDto;
import com.practice.interfaces.rest.dtos.SavedEmployeeDto;
import com.practice.interfaces.rest.validators.FieldCriteriaValidator;
import com.practice.interfaces.rest.validators.FieldCriteriaRule;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Employee Management API")
@RestController
@RequestMapping("v1/employees")
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    private final EntityAssembler entityAssembler;

    private final DtoAssembler dtoAssembler;

    private final FieldCriteriaValidator fieldCriteriaValidator;

    public EmployeeController(EmployeeService employeeService, EntityAssembler entityAssembler,
                                DtoAssembler dtoAssembler, FieldCriteriaValidator fieldCriteriaValidator) {
        this.employeeService = employeeService;
        this.entityAssembler = entityAssembler;
        this.dtoAssembler = dtoAssembler;
        this.fieldCriteriaValidator = fieldCriteriaValidator;
    }

    @ApiOperation("View a list of available employees")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded to retrieve a list of employees",
                        response = List.class),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to retrieve a list of employees")
    })
    @GetMapping
    public List<SavedEmployeeDto> getEmployees() {
        return employeeService.getEmployees();
    }

    @ApiOperation("Retrieve employee by field criteria")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded to retrieve an employee by field criteria",
                        response = SavedEmployeeDto.class),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Employee not found"),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_IMPLEMENTED, message = "Not supported field criteria is used"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid field criteria format")
    })
    @GetMapping("criteria/{fieldCriteria}")
    public SavedEmployeeDto getEmployeeByFieldCriteria(@PathVariable @FieldCriteriaRule String fieldCriteria) {
        String[] criterionTokens = fieldCriteria.split(":");
        if (criterionTokens != null && criterionTokens.length == 2) {
            String fieldName = criterionTokens[0].toLowerCase();
            String fieldValue = criterionTokens[1];
            if (!fieldCriteriaValidator.isValid(fieldName)) {
                throw new UnsupportedOperationException("Invalid criteria allowed criteria are id, email and username in the form of fieldName:validValue");
            }
            return dtoAssembler.toDto(employeeService.getEmployeeByFieldCriteria(fieldName, fieldValue), SavedEmployeeDto.class);
        } else {
            throw new IllegalArgumentException("Invalid criteria format, it should be in the form of fieldName:fieldValue");
        }
    }

    @ApiOperation("Create a new employee")
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

    @ApiOperation("Update employee's email by id")
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

    @ApiOperation("Delete employee by id")
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

    @ApiOperation("Register for scheduled mail alerts")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_ACCEPTED, message = "The request of scheduled alert is registered and will be processed later"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Rate alert payload contains invalid value"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to accept the request of scheduled alert")
    })
    @PostMapping("alerts")
    public ResponseEntity<Void> registerForScheduledMailAlert(@RequestBody @Valid RateAlertDto rateAlertDto) {
        employeeService.registerForScheduledMailAlert(entityAssembler.toEntity(rateAlertDto, RateAlert.class));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
