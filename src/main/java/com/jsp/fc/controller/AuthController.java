package com.jsp.fc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.serviceimpl.AuthServiceImpl;
import com.jsp.fc.util.ResponseStructure;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value = "/fp/v1")
@AllArgsConstructor
public class AuthController {
	
	private AuthService authService;
	@PostMapping(value="/register-user")
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@RequestBody UserRequest userRequest)
	{
		return authService.registerUser(userRequest);
	}
	
	@PostMapping(value ="/verify-otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(@RequestBody OtpModel otpModel)
	{
		return authService.verifyotp(otpModel);
	}
	

}
