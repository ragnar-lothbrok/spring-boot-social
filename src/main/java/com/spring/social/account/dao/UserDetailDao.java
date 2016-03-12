package com.spring.social.account.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.spring.social.account.model.UserDetail;

@Repository
public interface UserDetailDao extends CrudRepository<UserDetail, String> {

	/**
	 * This method will return an Account which is associated with the given
	 * Email.
	 * 
	 * @param emailId
	 * @return Account
	 */
	UserDetail findAccountByEmailId(String emailId);

	/**
	 * If we find any rows which is associated with account.
	 * 
	 * @param phoneNumber
	 * @return
	 */
	UserDetail findAccountByPhoneNumber(String phoneNumber);

}
