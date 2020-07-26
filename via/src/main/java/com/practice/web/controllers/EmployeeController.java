package com.practice.web.controllers;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.practice.services.AlertSchedularService;
import com.practice.services.EmployeeService;
import com.practice.transfomers.DtoTransformer;
import com.practice.web.dtos.EmailDto;
import com.practice.web.dtos.EmployeeDto;
import com.practice.web.dtos.RateAlertDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Employee Management API")
@RestController
@RequestMapping(value = "api/v1/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AlertSchedularService alertSchedularService;

    @Autowired
    private DtoTransformer dtoTransformer;

    @ApiOperation(value = "View a list of available employees")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded to retrieve a list of employees", response = List.class),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to retrieve a list of employees")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EmployeeDto> getEmployees() {
        return employeeService.getEmployees()
                                .stream()
                                .map(employee -> dtoTransformer.toDto(employee, EmployeeDto.class))
                                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Retrieve employee by field criteria")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded to retrieve an employee by field criteria", response = EmployeeDto.class),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Employee not found"),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_IMPLEMENTED, message = "Not supported field criteria is used"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid field criteria format")
    })
    @GetMapping(path = "criteria/{field}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeDto getEmployeeByFieldCriteria(@PathVariable("field") final String fieldCriteria) {
        return dtoTransformer.toDto(employeeService.getEmployeeByFieldCriteria(fieldCriteria), EmployeeDto.class);
    }

    @ApiOperation(value = "Create a new employee")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Succeeded to create a new employee"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Employee payload contains invalid value"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to create a new employee")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createEmployee(@RequestBody @Valid final EmployeeDto employeeDto) {
        return ResponseEntity.created(ServletUriComponentsBuilder
                                        .fromCurrentRequest()
                                        .path("/{id}")
                                        .build(employeeService.createEmployee(employeeDto)))
                                .build();
    }

    @ApiOperation(value = "Register for scheduled mail alerts")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_ACCEPTED, message = "The request of scheduled alert is registered and will be processed later"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Rate alert payload contains invalid value"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to accept the request of scheduled alert")
    })
    @PostMapping(path = "alerts/rates", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerForScheduledMailAlert(@RequestBody @Valid final RateAlertDto rateAlertDto) {
        alertSchedularService.registerForScheduledMailAlert(rateAlertDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @ApiOperation(value = "Update employee's email by id")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Succeeded to update employee's email"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Failed to update employee's email"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to update employee's email")
    })
    // partial update for the employee resource instead of using PUT that do a full resource update
    @PatchMapping(path = "{id:[0-9]+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateEmployeeEmailById(@PathVariable("id") final long id, @RequestBody @Valid final EmailDto emailDto) {
        employeeService.updateEmployeeEmailById(id, emailDto.getEmail());
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Delete employee by id")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Succeeded to delete employee by id"),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Requested employee for delete doesn't exist"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to delete employee by id")
    })
    @DeleteMapping(path = "{id:[0-9]+}")
    public ResponseEntity<?> deleteEmployeeById(@PathVariable("id") final long id) {
        employeeService.deleteEmployeeById(id);
        return ResponseEntity.noContent().build();
    }

}
