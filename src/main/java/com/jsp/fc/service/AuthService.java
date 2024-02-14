package com.jsp.fc.service;

import org.springframework.http.ResponseEntity;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.requestdto.AuthRequest;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.AuthResponse;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.util.ResponseStructure;
import com.jsp.fc.util.SimpleStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest);

	ResponseEntity<ResponseStructure<UserResponse>> verifyotp(OtpModel otpModel);

	ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest, HttpServletResponse httpServletResponse, String accessToken, String refereshToken);

	//ResponseEntity<ResponseStructure<String>> logOut(HttpServletResponse response, HttpServletRequest request);

	ResponseEntity<SimpleStructure> logOut(String accessToken, String refereshToken,
			HttpServletResponse httpServletResponse);


	ResponseEntity<SimpleStructure> revokeAcess(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

	ResponseEntity<SimpleStructure> revokeOtherAcess(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

	ResponseEntity<SimpleStructure> refreshToken(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse);

}
