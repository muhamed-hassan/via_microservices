package com.practice.web.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.practice.configs.constants.Messages;
import com.practice.configs.constants.Patterns;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Rate alert's payload details", value = "Rate Alert")
public class RateAlertDto extends BaseDto {

    @ApiModelProperty(value = "Base", required = true, example = "HUF")
    @NotNull(message = Messages.BASE_RATE_IS_MISSING)
    @Pattern(regexp = Patterns.CURRENCY_CODE_PATTERN, message = Messages.CURRENCY_CODE_SHOULD_BE_3_LETTERS)
    private String base;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
