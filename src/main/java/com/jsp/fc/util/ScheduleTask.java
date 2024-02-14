package com.jsp.fc.util;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.repository.AccessTokenRepo;
import com.jsp.fc.repository.RefreshTokenRepo;
import com.jsp.fc.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScheduleTask {

	UserRepository userRepository;
	AccessTokenRepo accessTokenRepo;
	RefreshTokenRepo refreshTokenRepo;
	
	
//	 @Scheduled(fixedDelay = 1000l * 60 * 60)
//		 private void deletUsers() {
//		 userRepository.findAll().forEach(user -> {
//		 if (!user.isDeleted())
//		 userRepository.delete(user);
//		
//		 });
//		 }
	
	
	@Scheduled(fixedDelay = 1000l*60*60)
	private void deletExpiredTokens() 
	{
		List<AccessToken> atokens=accessTokenRepo.findByExpirationBefore(LocalDateTime.now());
		for (AccessToken accessToken : atokens) {
			accessTokenRepo.delete(accessToken);
		}
		
		List<RefreshToken> rtokens=refreshTokenRepo.findByExpirationBefore(LocalDateTime.now());
		for (RefreshToken accessToken : rtokens) {
			refreshTokenRepo.delete(accessToken);
		}
	}
	
	
	
	
}
