package com.spring.social.account.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SocialUserDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	private String email;
	private String accessToken;
	private String imsId;
	private String socialType;

	public SocialUserDetail(String accessToken, String imsId, String socialType) {
		super();
		this.accessToken = accessToken;
		this.imsId = imsId;
		this.socialType = socialType;
	}

	public SocialUserDetail() {
	}
}
