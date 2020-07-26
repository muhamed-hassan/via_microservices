package com.practice.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.practice.configs.constants.Messages;
import com.practice.configs.constants.Patterns;
import com.practice.configs.constants.Rules;
import com.practice.web.dtos.EmployeeDto;

@Table(name = "employee",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = "username", name = EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME),
            @UniqueConstraint(columnNames = "email", name = EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL),
            @UniqueConstraint(columnNames = "phone_number", name = EmployeeEntity.Constraints.EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER)
        }
)
@Entity
public class EmployeeEntity {

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

    // for example we're going to use the Hungarian telephone numbers  ex: (06 29 123456) (06+areacode(1 to 2 digits)+telephonenumber(6 digits))
    @NotNull(message = Messages.PHONE_NUMBER_IS_MISSING)
    @Pattern(regexp = Patterns.PHONE_NUMBER_PATTERN, message = Messages.INVALID_PHONE_NUMBER)
    @Column(name = "phone_number")
    private String phoneNumber;

    // assuming start age for working is 22, and 59 is last year of working before retirement
    @Min(value = Rules.MIN_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_GE_22)
    @Max(value = Rules.MAX_AGE_FOR_WORKING, message = Messages.AGE_SHOULD_BE_LE_59)
    @Column
    private int age;

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(username)
            .append(email)
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
            .append(email, rhs.getEmail())
            .append(phoneNumber, rhs.getPhoneNumber())
            .isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static final class Constraints {

        public static final String EMPLOYEE_UNIQUE_CONSTRAINT_USERNAME = "employee_unique_username";
        public static final String EMPLOYEE_UNIQUE_CONSTRAINT_EMAIL = "employee_unique_email";
        public static final String EMPLOYEE_UNIQUE_CONSTRAINT_PHONE_NUMBER = "employee_unique_phone_number";

        private Constraints() {}

    }

}
