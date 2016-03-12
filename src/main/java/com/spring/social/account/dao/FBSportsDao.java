package com.spring.social.account.dao;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.social.account.model.FBSports;

@Repository
public interface FBSportsDao extends CrudRepository<FBSports, Long> {

	@Modifying
	@Transactional
	@Query("DELETE FROM FBSports WHERE userFaceBookId = :userFacebookId ")
	void deleteByUserFaceBookId(@Param("userFacebookId") String userFacebookId);
}
