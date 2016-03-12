package com.spring.social.exception.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This class will catch the AccountValidationExcepiton and send that in Json
 * format.
 * 
 * @author raghunandangupta
 *
 */
@ControllerAdvice
public class AbstractExceptionHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionHandler.class);

	/**
	 * Method will fetch Error descriptions and send them in json format.
	 * 
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<String> processAccountValidationException(Exception ex) {
		LOGGER.error("Exception occured : " + ex.getMessage());
		return new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

}
