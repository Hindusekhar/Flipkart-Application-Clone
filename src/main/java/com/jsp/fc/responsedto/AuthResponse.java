package com.jsp.fc.responsedto;

import java.time.LocalDateTime;

import com.jsp.fc.enums.UserRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class AuthResponse {

	private int userId;
	private String userName;
	private String role;
	private boolean isAuthenticated;
	private LocalDateTime accessExpiration;
	private LocalDateTime refreshExpiration;
}
