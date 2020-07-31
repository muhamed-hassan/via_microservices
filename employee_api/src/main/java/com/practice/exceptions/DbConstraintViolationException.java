package com.practice.exceptions;

public class DbConstraintViolationException extends RuntimeException {

	public DbConstraintViolationException(String message, Throwable cause) {
		super(message, cause);
	}

}
