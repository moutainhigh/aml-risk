/*
 * Copyright (c) 2018. LOLC Technology Services Ltd.
 * Author: Ranjith Kodikara
 * Date: 12/12/18 10:45
 */

package com.loits.aml.core;

import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.List;

/**
 * 
 * Based on BaseException, this is a more customized exception 
 * <br>
 * This should be modified to include action plan for each constructor
 * 
 * @author ranjithk
 * @since 2018-12-13
 * @version 1.0
 * @edited minolid@i360.lk
 * 
 */
public class FXDefaultException extends BaseException {

	/**
	 * 
	 */
	public FXDefaultException(String errorCode, String errorShortDescription, Date errorDate) {
		super(errorCode, errorShortDescription, errorDate);

	}

	public FXDefaultException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FXDefaultException(String errorCode, String errorShortDescription, String errorDescription, Date errorDate) {
		super(errorCode, errorShortDescription, errorDescription, errorDate);
		// TODO Auto-generated constructor stub
	}
	public FXDefaultException(String errorCode, String errorShortDescription, String errorDescription, Date errorDate, HttpStatus htst, Boolean severity) {
		super(errorCode, errorShortDescription, errorDescription, errorDate, htst, severity);
		// TODO Auto-generated constructor stub
	}

	public FXDefaultException(String errorCode, String errorShortDescription, String errorDescription, Date errorDate, HttpStatus htst) {
		super(errorCode, errorShortDescription, errorDescription, errorDate, htst);
		// TODO Auto-generated constructor stub
	}

	public FXDefaultException(HttpStatus status, String errorShortDescription, String errorDescription, List<String> errors) {
		super(status, errorShortDescription, errorDescription, errors);
	}
}

