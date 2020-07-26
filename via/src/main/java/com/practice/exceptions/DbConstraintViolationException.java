package com.practice.exceptions;

@SuppressWarnings("serial")
public class DbConstraintViolationException extends RuntimeException {

	public DbConstraintViolationException(String message, Throwable cause) {
		super(message, cause);
	}

}
