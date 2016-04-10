package com.spring.social.account.model;

public class MyAccessToken {
	private String token;
	private String tokensecret;

	public String getTokensecret() {
		return tokensecret;
	}

	public void setTokensecret(String tokensecret) {
		this.tokensecret = tokensecret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public MyAccessToken() {

	}

	public MyAccessToken(String token, String tokensecret) {
		super();
		this.token = token;
		this.tokensecret = tokensecret;
	}

}
