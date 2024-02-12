package com.jsp.fc.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.jsp.fc.exception.UserNotLoggedException;


@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {
	private ResponseEntity<Object> responseStructure(HttpStatus status, String message, Object rootCause) {
		return new ResponseEntity<Object>(Map.of("status", status.value(), "message", message, "rootCause", rootCause),
				status);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<ObjectError> allErrors = ex.getAllErrors();

		Map<String, String> errors = new HashMap<String, String>();
		allErrors.forEach(error -> {
			FieldError fieldError = (FieldError) error;
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		});
		return responseStructure(HttpStatus.BAD_REQUEST, "failed to save the userdate", errors);
	}

	@ExceptionHandler(UserNotLoggedException.class)
	public ResponseEntity<Object> handlerOfDuplicateEntryFoundException(UserNotLoggedException ex) {

		return responseStructure(HttpStatus.BAD_REQUEST, ex.getMessage(), "user not logged in ");

	}

	

}

