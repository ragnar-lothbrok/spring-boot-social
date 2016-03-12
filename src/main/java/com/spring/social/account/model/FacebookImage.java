package com.spring.social.account.model;

public class FacebookImage {

	private Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Facebook Image [data = " + data + "]";
	}
}
