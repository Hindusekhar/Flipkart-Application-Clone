package com.jsp.fc.security;

import java.security.Key;
import java.time.LocalTime;
import java.util.Base64.Decoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${myapp.secret}")
	private String secret;
	
	@Value("${myapp.access.expiry}")
	private Long accessExpirationInseconds;

	@Value("${myapp.refresh.expiry}")
	private Long refreshExpirationInSeconds;
	
	public String generateAccessToken(String userName) {
		return generateJWT(new HashMap<String, Object>(), userName,accessExpirationInseconds*1000l);
	}

	public String generateRefreshToken(String userName) {
		return generateJWT(new HashMap<String, Object>(), userName,refreshExpirationInSeconds*1000l);
	}
	
	private String generateJWT(Map<String, Object> claims,String userName,Long expiry) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(userName)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis()+accessExpirationInseconds*1000l))
				.signWith(getSignature(), SignatureAlgorithm.HS512)
				.compact();
	}
	private  Key getSignature() 
	{
		byte[] secretBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(secretBytes);
		
	}
	public Claims jwtParsert(String token) 
	{
		JwtParser jwtParser = Jwts.parserBuilder()
		.setSigningKey(getSignature())
		.build();
		return jwtParser.parseClaimsJws(token).getBody();
		
	}
	
	public String extractUserName(String token)
	{
		String userName = jwtParsert(token).getSubject();
		return userName;
	}
}
