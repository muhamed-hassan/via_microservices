package com.practice.exceptions;

import com.practice.configs.constants.ExceptionMessages;

@SuppressWarnings("serial")
public class ResponseProcessingFailureException extends RuntimeException {

	public ResponseProcessingFailureException(Throwable cause) {
		super(ExceptionMessages.FAILED_TO_PROCESS_THE_RESPONSE, cause);
	}

}
