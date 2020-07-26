package com.practice.exceptions;

import com.practice.configs.constants.ExceptionMessages;

@SuppressWarnings("serial")
public class EntityModificationException extends RuntimeException {

	public EntityModificationException() {
		super(ExceptionMessages.FAILED_TO_UPDATE_THE_ENTITY);
	}

}
