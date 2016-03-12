package com.spring.social.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.spring.social.account.dao.UserDetailDao;
import com.spring.social.account.model.UserDetail;

@RestController
@RequestMapping("/userDetail")
public class UserDetailController {

	final static Logger logger = LoggerFactory.getLogger(UserDetailController.class);

	@Autowired
	private UserDetailDao userDetailDao;

	/**
	 * Method to get User Details via IMS Id
	 * 
	 * @param imsId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = { "/imsId/{imsId}/" }, method = { RequestMethod.GET }, consumes = {
			MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> getUserDetails(@PathVariable String imsId, HttpServletRequest request) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			UserDetail userDetail = userDetailDao.findOne(imsId);
			if (userDetail != null) {
				responseMap.put("status", HttpStatus.FOUND);
				responseMap.put("userDetail", userDetail);
			} else {
				responseMap.put("status", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error("Exception Occured " + e);
			responseMap.put("status", HttpStatus.BAD_REQUEST);
			responseMap.put("error", e.getMessage());
		}
		logger.info("Get User details : " + responseMap);
		return responseMap;
	}

	/**
	 * Method to update User Details via IMS Id
	 * 
	 * @param userId
	 * @param userProfile
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/imsId/{imsId}/", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> updateUserDetails(@PathVariable String imsId, @RequestBody UserDetail userDetail,
			HttpServletRequest request) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			UserDetail existingUserDetail = userDetailDao.findOne(imsId);
			if (existingUserDetail != null) {
				userDetailDao.save(userDetail);
				responseMap.put("userDetail", userDetail);
				responseMap.put("status", HttpStatus.FOUND);
			} else {
				userDetail.setImsId(imsId);
				userDetailDao.save(userDetail);
				responseMap.put("userDetail", userDetail);
				responseMap.put("status", HttpStatus.CREATED);
			}
		} catch (Exception exception) {
			logger.error("Exception Occured " + exception);
			responseMap.put("status", HttpStatus.BAD_REQUEST);
			responseMap.put("error", exception.getMessage());
		}
		logger.info("Update User details : " + responseMap + " imsId : " + imsId);
		return responseMap;
	}

}