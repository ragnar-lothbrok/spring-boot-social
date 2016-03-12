package com.spring.social.account.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FBSports {

	@Id
	@GeneratedValue
	private Long id;
	
	private String userFaceBookId;
	
	private String sportsId;
	
	private String sportsName;
}
