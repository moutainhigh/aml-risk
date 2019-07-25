package com.loits.aml.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.persistence.RollbackException;
import java.text.ParseException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Handling REST Errors.
 * <p>
 * Be specific as much as possible. It will help the API client. (Consumer)
 *
 * @author Lahiru Bandara - Infinitum360 | info@infinitum360.com
 * @version 1.0
 * @since 1.0
 */

@RestControllerAdvice
public class RestExceptionHandler {

  Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

  /**
   * Controller level validation error handling
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

    ex.printStackTrace();
    logger.error("Application validation error error " + ex.getMessage());

    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setDebugMessage(ex.getMessage());
    apiError.setMessage("Object property validation failed");
    apiError.setFieldError(ex.getBindingResult().getFieldErrors());
    apiError.setGlobalErrors(ex.getBindingResult().getGlobalErrors());
    return buildResponseEntity(apiError);
  }


  /**
   * Service layer error handling
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(LoitServiceException.class)
  protected ResponseEntity<Object> loitException(
          LoitServiceException ex) {
    ex.printStackTrace();
    logger.error("Transaction error handler");
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(ex.getMessage());
    apiError.setDebugMessage(ex.getDebugMessage());
    return buildResponseEntity(apiError);
  }


  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<Object> loitException(
          HttpMessageNotReadableException ex) {
    ex.printStackTrace();
    logger.error("Automatic body parsing failed. Please check your request body");
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(ex.getMessage());
    return buildResponseEntity(apiError);
  }


  /**
   * DB error handling
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(RollbackException.class)
  protected ResponseEntity<Object> loitException(
          RollbackException ex) {

    ex.printStackTrace();
    logger.debug(ex.getMessage());
    logger.error("DB rollback error handler");
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  /**
   * Date Parse error handling
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(ParseException.class)
  protected ResponseEntity<Object> loitException(
          ParseException ex) {
    ex.printStackTrace();
    logger.error("Parse Exception ");
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage("There was a date parsing error.");
    apiError.setDebugMessage(ex.getMessage());
    return buildResponseEntity(apiError);
  }


  /**
   * String to number conversion error handling
   *
   * @param ex
   * @return
   */
  @ExceptionHandler({NumberFormatException.class,
          ConversionFailedException.class})
  protected ResponseEntity<Object> loitException(
          Exception ex) {
    ex.printStackTrace();
    logger.error("Numberformat error handler");
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage("Your request contains invalid numbers (in Object properties)");
    apiError.setDebugMessage(ex.getMessage());
    return buildResponseEntity(apiError);
  }

  /**
   * JSON mapping exceptions
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(JsonMappingException.class)
  protected ResponseEntity<Object> loitException(
          JsonMappingException ex) {
    ex.printStackTrace();
    logger.error("JsonMapping error handler");
    ApiError apiError = new ApiError(BAD_REQUEST);
    apiError.setMessage("POJO could not be mapped to JSON");
    apiError.setDebugMessage(ex.getMessage());
    return buildResponseEntity(apiError);
  }


  @ExceptionHandler(Exception.class)
  public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
    ex.printStackTrace();
    logger.error("General error handler");
    ApiError apiError = new ApiError(INTERNAL_SERVER_ERROR);
    apiError.setMessage("Opps!, something went wrong. Please try again");
    ex.printStackTrace();
    apiError.setDebugMessage(ex.getMessage());
    return buildResponseEntity(apiError);
  }


  /**
   * Convert Custom error entity to an accepted error type
   *
   * @param apiError
   * @return
   */
  private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
    return new ResponseEntity<Object>(apiError, apiError.getStatus());
  }
}
