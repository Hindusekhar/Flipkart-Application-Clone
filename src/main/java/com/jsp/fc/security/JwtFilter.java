package com.jsp.fc.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.exception.NotFoundException;
import com.jsp.fc.repository.AccessTokenRepo;

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

	@Autowired
	private AccessTokenRepo accessTokenRepo;
	@Autowired
	private JwtService jwtService;

	private CustomUserDetailsService customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String at = null;
		String rt = null;

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("at"))
					at = cookie.getValue();

				if (cookie.getName().equals("rt"))
					at = cookie.getValue();
			}

			if (at == null && rt == null) {
				List<AccessToken> accessToken = accessTokenRepo.findByTokenAndIsBlocked(at, false);

				if (accessToken == null)
					throw new NotFoundException("Access token not found");

				else {

					log.info("Authenticating the token....?");
					String name = jwtService.extractUserName(at);

					if (name == null)
						throw new UsernameNotFoundException("user not found");
					UserDetails userDetails = customUserDetailsService.loadUserByUsername(name);
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(name, null,
							userDetails.getAuthorities());
					token.setDetails(new WebAuthenticationDetails(request));
					SecurityContextHolder.getContext().setAuthentication(token);
					log.info("authenticated sucessfully...");
				}
			}

		}
		filterChain.doFilter(request, response);
	}

}
