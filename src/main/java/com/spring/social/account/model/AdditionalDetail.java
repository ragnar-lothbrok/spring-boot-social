package com.spring.social.account.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class AdditionalDetail {

	@Id
	private String imsId;
	
	private String userFaceBookId;
	
	private String ageRange;
	
	private String relationShipStatus;
	
	private String currentLocationId;
	
	private String currentLocation;
	
	private String accessToken;
	
	private String about;
	
}
