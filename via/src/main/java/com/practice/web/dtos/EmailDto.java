package com.practice.web.dtos;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Email's payload details", value = "Email")
public class EmailDto extends BaseDto {

    public EmailDto() {}

    public EmailDto(String email) {
        super.setEmail(email);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
