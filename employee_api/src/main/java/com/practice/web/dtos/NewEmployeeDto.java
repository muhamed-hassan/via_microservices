package com.practice.web.dtos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "New Employee payload details")
public class NewEmployeeDto extends BaseEmployeeDto {


//    @Override
//    public int hashCode() {
//        return new HashCodeBuilder(17, 37)
//            .append(getUsername())
//            .append(getEmail())
//            .append(getPhoneNumber())
//            .toHashCode();
//    }
//
//    @Override
//    public boolean equals(Object other) {
//        if (this == other)
//            return true;
//        if (other == null || getClass() != other.getClass())
//            return false;
//        NewEmployeeDto that = (NewEmployeeDto) other;
//        return new EqualsBuilder()
//            .append(getUsername(), that.getUsername())
//            .append(getEmail(), that.getEmail())
//            .append(getPhoneNumber(), that.getPhoneNumber())
//            .isEquals();
//    }

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
//        public NewEmployeeDto build() {
//            NewEmployeeDto employeeDto = new NewEmployeeDto();
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
