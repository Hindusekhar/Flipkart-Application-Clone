package com.jsp.fc.controller;

import javax.management.modelmbean.RequiredModelMBean;

import org.aspectj.apache.bcel.classfile.Module.Require;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.requestdto.AuthRequest;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.AuthResponse;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.serviceimpl.AuthServiceImpl;
import com.jsp.fc.util.ResponseStructure;
import com.jsp.fc.util.SimpleStructure;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	@PostMapping(value="/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest,HttpServletResponse httpServletResponse
			,@CookieValue( name = "at", required = false) String accessToken,@CookieValue( name = "rt", required = false) String refereshToken)
	{
		return authService.login(authRequest,httpServletResponse,accessToken,refereshToken);
	}

	@PostMapping(value="/logout")
	public ResponseEntity<SimpleStructure> logOut(@CookieValue( name = "at", required = false) String accessToken
			,@CookieValue( name = "rt", required = false) String refereshToken,HttpServletResponse httpServletResponse) 
	{
		return authService.logOut(accessToken,refereshToken,httpServletResponse);

	}

	@PostMapping(value = "/revoke-all")
	public ResponseEntity<SimpleStructure> revokeAllAcess(@CookieValue( name = "at", required = false) String accessToken
			,@CookieValue( name = "rt", required = false) String refreshToken,HttpServletResponse httpServletResponse)
	{
		return authService.revokeAcess(accessToken,refreshToken,httpServletResponse);
	}

	@PostMapping(value = "/revoke-other")
	public ResponseEntity<SimpleStructure> revokeOtherAcess(@CookieValue( name = "at", required = false) String accessToken
			,@CookieValue( name = "rt", required = false) String refreshToken,HttpServletResponse httpServletResponse)
	{
		return authService.revokeOtherAcess(accessToken,refreshToken,httpServletResponse);
	}

	@PostMapping(value = "/refresh")
	public ResponseEntity<SimpleStructure> refreshToken(@CookieValue( name = "at", required = false) String accessToken
			,@CookieValue( name = "rt", required = false) String refreshToken,HttpServletResponse httpServletResponse)
	{
		return authService.refreshToken(accessToken,refreshToken,httpServletResponse);
	}
}
