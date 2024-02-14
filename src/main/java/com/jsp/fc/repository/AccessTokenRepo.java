package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface AccessTokenRepo extends JpaRepository<AccessToken, Long> {
	
	Optional<AccessToken> findByToken(String token);

	//void findbyTokenAndIsBlocked(String at, boolean b);
	
	//Optional<AccessToken> findByTokenAndIsBlocked(String token, boolean blocked);

	List<AccessToken> findByExpirationBefore(LocalDateTime now);

	List<AccessToken> findAllByUserAndIsBlockedAndAndTokenNot(User user, boolean b, String accessToken);

	List<AccessToken> findByTokenAndIsBlocked(String at, boolean b);

	
	
}
