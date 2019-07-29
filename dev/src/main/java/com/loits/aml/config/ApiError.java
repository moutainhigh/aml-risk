package com.loits.aml.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class ApiError {

  private HttpStatus status;
  @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
  private Date timestamp;
  private String message;
  private String debugMessage;
  private List<FieldError> fieldError;
  private List<ObjectError> globalErrors;

  private ApiError() {
    timestamp = new Date();
  }

  public ApiError(HttpStatus status) {
    this();
    this.status = status;
  }

  public ApiError(HttpStatus status, Throwable ex) {
    this();
    this.status = status;
    this.message = "Unexpected error";
    this.debugMessage = ex.getLocalizedMessage();
  }

  public ApiError(HttpStatus status, String message, Throwable ex) {
    this();
    this.status = status;
    this.message = message;
    this.debugMessage = ex.getLocalizedMessage();
  }

  public List<ObjectError> getGlobalErrors() {
    return globalErrors;
  }

  public void setGlobalErrors(List<ObjectError> globalErrors) {
    this.globalErrors = globalErrors;
  }

  public List<FieldError> getFieldError() {
    return fieldError;
  }

  public void setFieldError(List<FieldError> fieldError) {
    this.fieldError = fieldError;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDebugMessage() {
    return debugMessage;
  }

  public void setDebugMessage(String debugMessage) {
    this.debugMessage = debugMessage;
  }
}
