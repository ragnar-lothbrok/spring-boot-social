package com.spring.social.account.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class FaceBookFriend {

	@Id
	@GeneratedValue
	private Long id;
	
	private String userFaceBookId;
}
