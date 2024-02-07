package com.jsp.fc.responsedto;

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
public class UserResponse {
	private int userId;
	private String userName;
	private String userEmail;
	private UserRole userRole;
	private boolean isEmailVerified;
	private boolean isDeleted;
}
