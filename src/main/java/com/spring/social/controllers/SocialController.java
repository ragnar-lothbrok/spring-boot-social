package com.spring.social.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.spring.social.account.constants.UserAPIConstants;
import com.spring.social.account.dto.SocialUserDetail;
import com.spring.social.security.SocialAccountFactory;

@RestController
@RequestMapping("/userSocialDetail")
public class SocialController {

	final static Logger logger = LoggerFactory.getLogger(SocialController.class);

	@Autowired
	SocialAccountFactory socialAccountFactory;

	/**
	 * Method to Create User Details
	 * 
	 * @param userId
	 * @param userProfile
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> createUserDetails(@RequestBody SocialUserDetail socialUserDetail,
			HttpServletRequest request) {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			if(UserAPIConstants.FACEBOOK.equalsIgnoreCase(socialUserDetail.getSocialType())
					|| UserAPIConstants.GOOGLE.equalsIgnoreCase(socialUserDetail.getSocialType())){
				if(UserAPIConstants.FACEBOOK.equalsIgnoreCase(socialUserDetail.getSocialType())){
					socialAccountFactory.getDetailsFromFacebook(socialUserDetail);
				}else if(UserAPIConstants.GOOGLE.equalsIgnoreCase(socialUserDetail.getSocialType())){
					socialAccountFactory.getDetailsFromGoogle(socialUserDetail);
				}
				responseMap.put("status", HttpStatus.ACCEPTED);
			}else{
				responseMap.put("status", HttpStatus.BAD_REQUEST);
			}
		} catch (Exception exception) {
			logger.error("Exception Occured " + exception);
			responseMap.put("status", HttpStatus.BAD_REQUEST);
			responseMap.put("error", exception.getMessage());
		}
		logger.info("Add User details : " + responseMap);
		return responseMap;
	}

}
