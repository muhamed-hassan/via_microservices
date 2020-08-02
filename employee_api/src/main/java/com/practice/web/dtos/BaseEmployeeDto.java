package com.practice.web.dtos;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.practice.configs.constants.Messages;
import com.practice.configs.constants.Patterns;
import com.practice.configs.constants.Rules;

import io.swagger.annotations.ApiModelProperty;

public class BaseEmployeeDto extends BaseDto {

    @ApiModelProperty(value = "Name", required = true, example = "name")
    @NotNull(message = Messages.NAME_IS_MISSING)
    @Pattern(regexp = Patterns.NAME_PATTERN, message = Messages.INVALID_NAME)
    private String name;

    @ApiModelProperty(value = "Username", required = true, example = "username")
    @NotNull(message = Messages.USER_NAME_IS_MISSING)
    @Pattern(regexp = Patterns.USER_NAME_PATTERN, message = Messages.INVALID_USER_NAME)
    private String username;

    @ApiModelProperty(value = "Phone Number", required = true, example = "(421)-5555")
    @NotNull(message = Messages.PHONE_NUMBER_IS_MISSING)
    @Pattern(regexp = Patterns.PHONE_NUMBER_PATTERN, message = Messages.INVALID_PHONE_NUMBER)
    private String phoneNumber;

    @ApiModelProperty(value = "Age", required = true, example = "22")
    @Min(value = Rules.MIN_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_GE_22)
    @Max(value = Rules.MAX_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_LE_59)
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
