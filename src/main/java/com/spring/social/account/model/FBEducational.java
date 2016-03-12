package com.spring.social.account.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FBEducational {

	@Id
	@GeneratedValue
	private Long id;
	
	private String userFaceBookId;
	
	private String schoolId;
	private String schoolName;
	
	private String degreeId;
	private String degreeName;
	
	private String type;
	
	private String conectrationId;
	private String concentrationName;
	
	private String yearId;
	private String yearName;
	
}
