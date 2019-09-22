/*
 * Copyright (c) 2018. LOLC Technology Services Ltd.
 * Author: Ranjith Kodikara
 * Date: 12/12/18 10:45
 */

package com.loits.aml.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.Optional;

/**
 * 
 * This will return the relevant object based on the caught exception
 * 
 * @author ranjithk
 * @since 2018-12-13
 * @version 1.0
 * 
 */
@RestControllerAdvice
public class BaseResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger logger_Breh = LoggerFactory.getLogger(BaseResponseEntityExceptionHandler.class);

	/**
	 * 
	 * @param ex      AccessDeniedException relevant exception
	 * @param request WebRequest request should be passed as the second param
	 * @return FXDefaultException
	 * @author ranjithk 2018-12-13 Added this to handle
	 * 
	 * 
	 */
	@ExceptionHandler({ AccessDeniedException.class })
	public ResponseEntity<FXDefaultException> handleAccessDeniedException(AccessDeniedException ex,
                                                                          WebRequest request) {
		logger_Breh.info("Acess denied:" + request.REFERENCE_REQUEST + request.toString() + " on "
				+ new Date().toString());
		System.out.println(("Acess denied in BaseResponseEntityExceptionHandler.handleAccessDeniedException:"
				+ request.REFERENCE_REQUEST + request.toString() + " on " + new Date().toString()));
		FXDefaultException fxd = new FXDefaultException("1000", "Access denied:" + ex.getMessage(),
				ex.getLocalizedMessage(), new Date());
		return new ResponseEntity<FXDefaultException>(fxd, new HttpHeaders(), HttpStatus.FORBIDDEN);
	}

	private ResponseEntity<FXDefaultException> createError(String errorCode, String errorShortDescription,
                                                           final Exception ex, final HttpStatus httpStatus, final String logRef) {
		final String message = Optional.of(ex.getMessage()).orElse(ex.getClass().getSimpleName());
		FXDefaultException fxd = new FXDefaultException(errorCode, errorShortDescription, message, new Date(),
				httpStatus);
		fxd.setStackTrace(ex.getStackTrace());
		return new ResponseEntity<>(fxd, httpStatus);
	}

	/**
	 *
	 * @param IllegalArgumentException or IllegalStateException ex relevant
	 *                                 exception
	 * @param WebRequest               request request should be passed as the
	 *                                 second param
	 * @return ResponseEntity<Object>
	 * @author ranjithk 2018-12-13 Added this to handle
	 *
	 *
	 */
	@ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
	protected ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
		String bodyOfResponse = "This should be application specific";
		return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request);
	}

	/**
	 *
	 * @param FXDefaultException ex relevant exception
	 * @param WebRequest         request request should be passed as the second
	 *                           param
	 * @return FXDefaultException
	 * @author ranjithk 2018-12-13 Added this to handle
	 *
	 *
	 */
//	@ExceptionHandler(FXDefaultException.class)
//	protected ResponseEntity<FXDefaultException> handleFXDefaultException(FXDefaultException ex, WebRequest request) {
//		System.out.println("in BaseResponseEntityExceptionHandler.handleAnyError()");
//		String errorCodeToShow = (ex.getErrorCode() == null) ? "1998" : ex.getErrorCode();
//
//		FXDefaultException fxd = new FXDefaultException(errorCodeToShow,
//				"Programattically handled Custom Abnormal condition : " + ex.getErrorShortDescription(),
//				ex.getErrorDescription(), new java.util.Date());
//		fxd.setSeverity(ex.getSeverity());
//		fxd.setStackTrace(ex.getStackTrace());
//		errorCodeToShow = null;
//		return createError("9998", "HANDLED_ERROR", ex, HttpStatus.INTERNAL_SERVER_ERROR, "Handled Error.");
//		//return new ResponseEntity<FXDefaultException>(fxd, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
//	}

	/**
	 *
	 * @param Exception  ex relevant exception
	 * @param WebRequest request request should be passed as the second param
	 * @return FXDefaultException
	 * @author ranjithk 2018-12-13 Added this to handle
	 *
	 *
	 */
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<FXDefaultException> handleAnyError(Exception ex, WebRequest request) {
		System.out.println("in BaseResponseEntityExceptionHandler.handleAnyError()");
		FXDefaultException fxd = new FXDefaultException("1999", "Unhandled error:" + ex.getMessage(),
				ex.getLocalizedMessage(), new Date());
		fxd.setStackTrace(ex.getStackTrace());
		return createError("9999", "UNHANDLED_ERROR", ex, HttpStatus.INTERNAL_SERVER_ERROR, "Unhandled Error.");
		// return new ResponseEntity<FXDefaultException>(fxd, new HttpHeaders(),
		// HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(FXDefaultException.class)
	public ResponseEntity<Object> validateRecordException(FXDefaultException ex, WebRequest request){
		ex.setStackTrace(new StackTraceElement[0]);
		return new ResponseEntity<Object>(ex, ex.getHttpStatus());
	}

	@ExceptionHandler(CannotCreateTransactionException.class)
	public ResponseEntity<Object> cannotCreateTransactionException(CannotCreateTransactionException ex, WebRequest request){
		ex.setStackTrace(new StackTraceElement[0]);
		return new ResponseEntity<Object>(ex, HttpStatus.BAD_REQUEST);
	}

}

