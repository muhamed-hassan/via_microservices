package com.practice.interfaces.rest.dtos;

import java.util.Objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Saved Employee payload details")
public class SavedEmployeeDto extends BaseEmployeeDto {

    @ApiModelProperty(value = "ID", readOnly = true, hidden = true)
    private long id;

    public SavedEmployeeDto() {}

    public SavedEmployeeDto(long id, String name, String username, String email, String phoneNumber, int age) {
        this.id = id;
        setName(name);
        setUsername(username);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAge(age);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SavedEmployeeDto that = (SavedEmployeeDto) other;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
