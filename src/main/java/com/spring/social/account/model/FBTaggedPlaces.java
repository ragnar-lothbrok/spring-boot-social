package com.spring.social.account.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FBTaggedPlaces {

	@Id
	@GeneratedValue
	private Long id;

	private String userFaceBookId;
	private String taggedId;

	private String taggedPlaceId;
	private String taggedPLaceName;
	private String locationId;
	private String locationName;
	private String latitude;
	private String longitude;
	private String street;
	private String city;
	private String country;
	private String zip;
}
