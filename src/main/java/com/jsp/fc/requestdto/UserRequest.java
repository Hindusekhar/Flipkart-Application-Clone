package com.jsp.fc.requestdto;

import com.jsp.fc.entity.User;
import com.jsp.fc.enums.UserRole;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
	
private int userId;
	private String userEmail;
	private String password;
	private UserRole userRole;
	
	
	
}
