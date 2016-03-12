package com.spring.social.account.model;

import lombok.Data;

@Data
public class Errors {

	private String errorCode;
	private String errorDescription;

	public Errors(String errorCode, String errorDescription) {
		super();
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
	}

}
