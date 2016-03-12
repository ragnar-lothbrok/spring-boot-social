package com.spring.social.account.dao;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.spring.social.account.model.FbFavouriteTeam;

public interface FbFavouriteTeamDao extends CrudRepository<FbFavouriteTeam, Long> {

	@Modifying
	@Transactional
	@Query("DELETE FROM FbFavouriteTeam WHERE userFaceBookId = :userFacebookId ")
	void deleteByUserFaceBookId(@Param("userFacebookId") String userFacebookId);
}

