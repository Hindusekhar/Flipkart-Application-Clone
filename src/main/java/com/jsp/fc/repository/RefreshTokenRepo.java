package com.jsp.fc.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.entity.User;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{

	List<RefreshToken> findByExpirationBefore(LocalDateTime now);

	List<RefreshToken> findAllByUserAndIsBlockedAndAndTokenNot(User user, boolean b, String refreshToken);

	Optional<RefreshToken> findByToken(String token);



}
