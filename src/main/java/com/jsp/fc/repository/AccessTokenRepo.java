package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.AccessToken;
import java.util.List;
import java.util.Optional;


public interface AccessTokenRepo extends JpaRepository<AccessToken, Long> {
	
	Optional<AccessToken> findByToken(String token);

	//void findbyTokenAndIsBlocked(String at, boolean b);
	
	Optional<AccessToken> findByTokenAndIsBlocked(String token, boolean blocked);

}
