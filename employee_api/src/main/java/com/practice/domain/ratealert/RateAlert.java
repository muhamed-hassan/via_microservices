package com.practice.domain.ratealert;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.practice.infrastructure.configs.constants.Messages;
import com.practice.infrastructure.configs.constants.Patterns;

@Table(name = "rate_alert")
@Entity
public class RateAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = Messages.BASE_RATE_IS_MISSING)
    @Pattern(regexp = Patterns.CURRENCY_CODE_PATTERN, message = Messages.CURRENCY_CODE_SHOULD_BE_3_LETTERS)
    @Column
    private String base;

    @NotNull(message = Messages.EMAIL_IS_MISSING)
    @Email(message = Messages.EMAIL_IS_INVALID)
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        RateAlert that = (RateAlert) other;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
