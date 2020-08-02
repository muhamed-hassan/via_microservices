package com.practice.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.practice.configs.constants.Messages;
import com.practice.configs.constants.Patterns;
import com.practice.configs.constants.Rules;
import com.practice.web.dtos.SavedEmployeeDto;

@Table(name = "employee")
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = Messages.NAME_IS_MISSING)
    @Pattern(regexp = Patterns.NAME_PATTERN, message = Messages.INVALID_NAME)
    @Column
    private String name;

    @NotNull(message = Messages.USER_NAME_IS_MISSING)
    @Pattern(regexp = Patterns.USER_NAME_PATTERN, message = Messages.INVALID_USER_NAME)
    @Column
    private String username;

    @NotNull(message = Messages.EMAIL_IS_MISSING)
    @Email(message = Messages.EMAIL_IS_INVALID)
    @Column
    private String email;

    @NotNull(message = Messages.PHONE_NUMBER_IS_MISSING)
    @Pattern(regexp = Patterns.PHONE_NUMBER_PATTERN, message = Messages.INVALID_PHONE_NUMBER)
    @Column(name = "phone_number")
    private String phoneNumber;

    @Min(value = Rules.MIN_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_GE_22)
    @Max(value = Rules.MAX_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_LE_59)
    @Column
    private int age;

    @Version
    private int version;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

}
