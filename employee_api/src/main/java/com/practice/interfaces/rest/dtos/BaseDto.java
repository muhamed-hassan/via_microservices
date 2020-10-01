package com.practice.interfaces.rest.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import com.practice.infrastructure.configs.constants.Messages;

import io.swagger.annotations.ApiModelProperty;

public class BaseDto {

    @ApiModelProperty(value = "Email", required = true, example = "test@test.com")
    @NotNull(message = Messages.EMAIL_IS_MISSING)
    @Email(message = Messages.EMAIL_IS_INVALID)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
