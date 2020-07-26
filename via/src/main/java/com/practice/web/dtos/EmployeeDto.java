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

@ApiModel(description = "Employee's payload details", value = "Employee")
public class EmployeeDto extends BaseDto {

    @ApiModelProperty(value = "ID", readOnly = true, required = false, hidden = true)
    private long id; //optional in case of posting

    @ApiModelProperty(value = "Name", required = true, example = "name")
    @NotNull(message = Messages.NAME_IS_MISSING)
    @Pattern(regexp = Patterns.NAME_PATTERN, message = Messages.INVALID_NAME)
    private String name;

    @ApiModelProperty(value = "Username", required = true, example = "username")
    @NotNull(message = Messages.USER_NAME_IS_MISSING)
    @Pattern(regexp = Patterns.USER_NAME_PATTERN, message = Messages.INVALID_USER_NAME)
    private String username;

    // for example we're going to use the Hungarian telephone numbers  ex: (06 29 123456) (06+areacode(1 to 2 digits)+telephonenumber(6 digits))
    @ApiModelProperty(value = "Phone Number", required = true, example = "061123456")
    @NotNull(message = Messages.PHONE_NUMBER_IS_MISSING)
    @Pattern(regexp = Patterns.PHONE_NUMBER_PATTERN, message = Messages.INVALID_PHONE_NUMBER)
    @JsonProperty(value = "phone_number")
    private String phoneNumber;

    // assuming start age for working is 22, and 59 is last year of working before retirement
    @ApiModelProperty(value = "Age", required = true, example = "22")
    @NotNull(message = Messages.AGE_IS_MISSING)
    @Min(value = Rules.MIN_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_GE_22)
    @Max(value = Rules.MAX_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_LE_59)
    private Integer age;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(username)
            .append(getEmail())
            .append(phoneNumber)
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        EmployeeDto rhs = (EmployeeDto) obj;
        return new EqualsBuilder()
            .append(id, rhs.getId())
            .append(username, rhs.getUsername())
            .append(getEmail(), rhs.getEmail())
            .append(phoneNumber, rhs.getPhoneNumber())
            .isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static class Builder {

        private String name;
        private String username;
        private String email;
        private String phoneNumber;
        private int age;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public EmployeeDto build() {
            EmployeeDto employeeDto = new EmployeeDto();
            employeeDto.setName(name);
            employeeDto.setUsername(username);
            employeeDto.setEmail(email);
            employeeDto.setPhoneNumber(phoneNumber);
            employeeDto.setAge(age);
            return employeeDto;
        }

    }

}
