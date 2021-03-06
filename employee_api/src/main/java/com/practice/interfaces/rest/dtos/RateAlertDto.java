package com.practice.interfaces.rest.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.practice.infrastructure.configs.constants.Messages;
import com.practice.infrastructure.configs.constants.Patterns;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Rate alert's payload details", value = "Rate Alert")
public class RateAlertDto extends BaseDto {

    @ApiModelProperty(value = "Base", required = true, example = "ISK")
    @NotNull(message = Messages.BASE_RATE_IS_MISSING)
    @Pattern(regexp = Patterns.CURRENCY_CODE_PATTERN, message = Messages.CURRENCY_CODE_SHOULD_BE_3_LETTERS)
    private String base;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

}
