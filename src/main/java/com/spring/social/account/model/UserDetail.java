package com.spring.social.account.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.social.facebook.api.User;
import org.springframework.social.google.api.plus.Person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spring.social.account.constants.UserAPIConstants;

import lombok.Data;

@Entity
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetail {
	public UserDetail(User userProfile, String password2) {
		this.setEmailId(userProfile.getEmail());
		this.setFirstName(userProfile.getFirstName());
		this.setLastName(userProfile.getLastName());
		this.setGender(userProfile.getGender());
		this.setPassword(password2);
		// this.setPhoneNumber(userProfile.get);
		try {
			if (userProfile.getBirthday() != null) {
				this.setDob(UserAPIConstants.SQL_DATE_FORMAT
						.format(UserAPIConstants.FACEBOOK_DATE_FORMAT.parse(userProfile.getBirthday())));
			}
		} catch (Exception exception) {

		}
		this.setSsoProvider("Facebook");
	}

	public UserDetail() {
	}

	public UserDetail(Person person) {
		this.setEmailId(person.getAccountEmail());
		this.setFirstName(person.getGivenName());
		this.setLastName(person.getFamilyName());
		this.setDob(person.getBirthday() == null ? null
				: UserAPIConstants.SQL_DATE_FORMAT.format(new Date(person.getBirthday().getTime())));
		this.setGender(person.getGender());
		this.setSsoProvider("Google");
		this.setProfileImage(person.getImageUrl());
	}

	@Id
	private String imsId;

	@Column(unique = true)
	private String emailId;

	private String firstName;
	private String lastName;
	private String gender;
	private String ssoProvider;

	@JsonIgnore
	@Column(name = "password_hash")
	private String password;

	private String dob;
	private String phoneNumber;

	private short isActive = 1;
	private String country;

	@JsonIgnore
	private String createDate;

	private String profileImage;

}
