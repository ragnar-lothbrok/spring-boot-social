package com.spring.social.account.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FBWorkEntry {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private String userFaceBookId;
	private String employeerId;
	private String locationId;
	private String positionId;
	
	private String employeeName;
	private String locationName;
	private String startDate;
	private String endDate;
	private String positionName;
}
