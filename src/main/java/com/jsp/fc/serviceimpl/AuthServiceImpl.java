package com.jsp.fc.serviceimpl;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jsp.fc.entity.Customer;
import com.jsp.fc.entity.Seller;
import com.jsp.fc.entity.User;
import com.jsp.fc.repository.CustomerRepository;
import com.jsp.fc.repository.SellerRepository;
import com.jsp.fc.repository.UserRepository;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.util.ResponseStructure;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
	private SellerRepository sellerRepository;
	private CustomerRepository customerRepository;
	private ResponseStructure<UserResponse> responseStructure;
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	

	private UserResponse mapToUserResponse(User request) {
		return UserResponse.builder().userId(request.getUserId()).userName(request.getUserName()).userEmail(request.getUserEmail()).userRole(request.getUserRole()).build();
	}

	private <T extends User> T mapToUser(UserRequest userRequest) {
		User user = null;
		
		switch (userRequest.getUserRole()) {
		case SELLER: {
			user = new Seller();
			break;
		}
		case CUSTOMER: {
			user = new Customer();
			break;
		}
		}
		user.setUserEmail(userRequest.getUserEmail());
		user.setPassword(  passwordEncoder.encode( userRequest.getPassword()));
		user.setUserRole(userRequest.getUserRole());
		user.setUserName(userRequest.getUserEmail().split("@")[0]);
		return (T) user;
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		User user=userRepository.findByUserName(userRequest.getUserEmail().split("@")[0]).map(u->{
			if(u.isEmailVerified())
				throw new RuntimeException("user verified");
			else
			{}		
			return u;
			
		}).orElseGet(()-> mapToSaveRespective(mapToUser(userRequest)));
		
		return new ResponseEntity<ResponseStructure<UserResponse>>(
				responseStructure.setStatus(HttpStatus.CREATED.value()).setData(mapToUserResponse(user))
						.setMessage("please verify emailid"),
				HttpStatus.CREATED);

	}

	private User mapToSaveRespective(User user) {
		switch (user.getUserRole()) {
		case SELLER -> {
			user = sellerRepository.save((Seller) user);
		}
		case CUSTOMER -> {
			user = customerRepository.save((Customer) user);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + user.getUserRole());
		}
		return user;
	}
	
	@Scheduled(fixedDelay = 1000l*60)
	private void deletUsers() 
	{
		userRepository.findAll().forEach(user->{
			if(!user.isDeleted())
				userRepository.delete(user);
				
		});

		
	}

}
