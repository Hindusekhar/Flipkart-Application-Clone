package com.jsp.fc.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jsp.fc.exception.NotFoundException;
import com.jsp.fc.exception.UserNotADMINException;
import com.jsp.fc.repository.UserRepository;

import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
			return userRepository.findByUserName(userName).map(user->new CustomUserDetails(user)).
			orElseThrow(()->new NotFoundException("user Not found"));
			
	}

}
