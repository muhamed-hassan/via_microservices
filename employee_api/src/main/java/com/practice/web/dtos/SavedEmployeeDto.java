package com.practice.web.dtos;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.configs.constants.Messages;
import com.practice.configs.constants.Patterns;
import com.practice.configs.constants.Rules;

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

    public SavedEmployeeDto(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        SavedEmployeeDto that = (SavedEmployeeDto) other;
        return new EqualsBuilder()
            .append(id, that.getId())
            .isEquals();
    }



//    public static class Builder {
//
//        private String name;
//        private String username;
//        private String email;
//        private String phoneNumber;
//        private int age;
//
//        public Builder name(String name) {
//            this.name = name;
//            return this;
//        }
//
//        public Builder username(String username) {
//            this.username = username;
//            return this;
//        }
//
//        public Builder email(String email) {
//            this.email = email;
//            return this;
//        }
//
//        public Builder phoneNumber(String phoneNumber) {
//            this.phoneNumber = phoneNumber;
//            return this;
//        }
//
//        public Builder age(int age) {
//            this.age = age;
//            return this;
//        }
//
//        public SavedEmployeeDto build() {
//            SavedEmployeeDto employeeDto = new SavedEmployeeDto();
//            employeeDto.setName(name);
//            employeeDto.setUsername(username);
//            employeeDto.setEmail(email);
//            employeeDto.setPhoneNumber(phoneNumber);
//            employeeDto.setAge(age);
//            return employeeDto;
//        }
//
//    }

}
