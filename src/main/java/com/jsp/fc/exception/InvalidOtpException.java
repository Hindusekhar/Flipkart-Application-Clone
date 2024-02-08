package com.jsp.fc.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvalidOtpException extends RuntimeException {
	private String message;
}
