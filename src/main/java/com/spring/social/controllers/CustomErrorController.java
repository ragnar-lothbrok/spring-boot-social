package com.spring.social.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ErrorController added to intercept the errors and return the appropriate
 * response.
 * 
 * @author raghunandangupta
 *
 */

@Controller
public class CustomErrorController implements ErrorController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorController.class);

	/**
	 * Simply returning status as false if login fails.
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/error")
	@ResponseBody
	public Map<String, Object> error(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		LOGGER.info("Inside Error Controller : " + req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
		if (req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) != null) {
			Map<String, Object> loginDetails = new HashMap<String, Object>();
			loginDetails.put("status", Boolean.FALSE);
			return loginDetails;
		}
		return null;
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
