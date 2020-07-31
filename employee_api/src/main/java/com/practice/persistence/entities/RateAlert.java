package com.practice.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.practice.configs.constants.Messages;
import com.practice.configs.constants.Patterns;

@Table(name = "rate_alert"/*,
        uniqueConstraints = @UniqueConstraint(columnNames = "email", name = RateAlert.Constraints.RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL)
*/)
@Entity
public class RateAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = Messages.BASE_RATE_IS_MISSING)
    @Pattern(regexp = Patterns.CURRENCY_CODE_PATTERN, message = Messages.CURRENCY_CODE_SHOULD_BE_3_LETTERS)
    @Column
    private String base;

    @NotNull(message = Messages.EMAIL_IS_MISSING)
    @Email(message = Messages.EMAIL_IS_INVALID)
//@EmailRule
    @Column
    private String email;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(base)
            .append(email)
            .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        if (other.getClass() != getClass()) {
            return false;
        }
        RateAlert that = (RateAlert) other;
        return new EqualsBuilder()
            .append(id, that.getId())
            .append(base, that.getBase())
            .append(email, that.getEmail())
            .isEquals();
    }


    public static final class Constraints {

        public static final String RATE_ALERT_UNIQUE_CONSTRAINT_EMAIL = "rate_alert_unique_email";

        private Constraints() {}

    }

}
