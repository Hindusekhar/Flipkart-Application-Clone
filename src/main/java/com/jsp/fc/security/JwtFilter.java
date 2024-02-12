package com.jsp.fc.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.exception.NotFoundException;
import com.jsp.fc.exception.UserNotLoggedException;
import com.jsp.fc.repository.AccessTokenRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private AccessTokenRepo accessTokenRepo;
	private JwtService jwtService;
	private CustomUserDetailsService customUserDetailsService;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String at=null;
		String rt=null;

		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if(cookie.getName().equals("at")) at=cookie.getValue();
			
				if(cookie.getName().equals("rt")) at=cookie.getValue();

		}
		String userName=null;
		if(at==null && rt==null) throw new UserNotLoggedException("user is not lgged ");
	Optional<AccessToken> accessToken = accessTokenRepo.findByTokenAndIsBlocked(at,false);
		
	if(accessToken==null)throw new NotFoundException("Access token not found");
	else {
		
		log.info("Authenticating the token....?");
		userName = jwtService.extractUserName(at);
		
		if(userName==null)throw new UsernameNotFoundException(userName);
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(userName);
		UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(userName, null,userDetails.getAuthorities());
		token.setDetails(new WebAuthenticationDetails(request));
		SecurityContextHolder.getContext().setAuthentication(token);
		log.info("authenticated sucessfully...");
	}
	filterChain.doFilter(request, response);
			
		
	}





}
