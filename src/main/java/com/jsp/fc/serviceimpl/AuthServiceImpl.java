package com.jsp.fc.serviceimpl;

import java.util.Date;
import java.util.Random;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jsp.fc.cache.CacheStore;
import com.jsp.fc.entity.Customer;
import com.jsp.fc.entity.Seller;
import com.jsp.fc.entity.User;
import com.jsp.fc.exception.ExpiredException;
import com.jsp.fc.exception.InvalidOtpException;
import com.jsp.fc.repository.CustomerRepository;
import com.jsp.fc.repository.SellerRepository;
import com.jsp.fc.repository.UserRepository;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.util.MessageStructure;
import com.jsp.fc.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.websocket.Encoder.Text;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
	private SellerRepository sellerRepository;
	private CustomerRepository customerRepository;
	private ResponseStructure<UserResponse> responseStructure;
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	private JavaMailSender javaMailSender;

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		if (userRepository.existsByUserEmail(userRequest.getUserEmail()))
			throw new RuntimeException("user already preset");

		String otp = generateOtp();
		User user = mapToUser(userRequest);

		otpCacheStore.add(userRequest.getUserEmail(), otp);
		userCacheStore.add(userRequest.getUserEmail(), user);

		try {
			sendOtpToMail(user, otp);
		} catch (MessagingException e) {

			e.printStackTrace();
			log.error("the mail adress doesn't exist");
		}

		return new ResponseEntity<ResponseStructure<UserResponse>>(
				responseStructure.setStatus(HttpStatus.CREATED.value()).setData(mapToUserResponse(user))
						.setMessage("please verify otp sent on email Id"),
				HttpStatus.CREATED);

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyotp(OtpModel otpModel) {
		User user = userCacheStore.get(otpModel.getUserEmail());
		String otp = otpCacheStore.get(otpModel.getUserEmail());

		if (otp == null)
			throw new ExpiredException("otp Expired");
		if (user == null)
			throw new ExpiredException("Registration session expired");
		if (!otp.equals(otpModel.getOtp()))
			throw new InvalidOtpException("Invalid Otp");

		user.setEmailVerified(true);
		user = userRepository.save(user);
		try {
			sendConfirmRegistration(user);
		} catch (MessagingException e) {
			log.error("the mail adress doesn't exist");
		}

		return new ResponseEntity<ResponseStructure<UserResponse>>(responseStructure
				.setStatus(HttpStatus.CREATED.value()).setMessage(otp).setData(mapToUserResponse(user)),
				HttpStatus.CREATED);
	}

	private UserResponse mapToUserResponse(User request) {
		return UserResponse.builder().userId(request.getUserId()).userName(request.getUserName())
				.userEmail(request.getUserEmail()).userRole(request.getUserRole()).build();
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
		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		user.setUserRole(userRequest.getUserRole());
		user.setUserName(userRequest.getUserEmail().split("@")[0]);
		return (T) user;
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

	// @Scheduled(fixedDelay = 1000l * 60 * 60)
	// private void deletUsers() {
	// userRepository.findAll().forEach(user -> {
	// if (!user.isDeleted())
	// userRepository.delete(user);
	//
	// });
	// }
	//
	private String generateOtp() {
		return String.valueOf(new Random().nextInt(100000, 999999));

	}

	public void sendConfirmRegistration(User user) throws MessagingException {
		sendMail(MessageStructure.builder().to(user.getUserEmail()).Subject("Successfully registered with Flipkart")
				.sentDate(new Date())
				.text("<h4> Thank You for Registering With Flipkart </h4>" + "<h2>" + user.getUserName() + "<h2>")
				.build());
	}

	public void sendOtpToMail(User user, String otp) throws MessagingException {
		sendMail(MessageStructure.builder().to(user.getUserEmail()).Subject("Complete your Registaration to Flipkart")
				.sentDate(new Date())
				.text("hey, " + user.getUserName() + "Good to see you interested in flipkart,"
						+ "complete your Registration using the otp<br>" + "<h1>" + otp + "</h1><br>"
						+ "Note: the otp expires in 1 minute" + "<br><br>" + "With Best Regards<br>" + "flipkart")
				.build());
	}

	@Async
	private void sendMail(MessageStructure messageStructure) throws MessagingException {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setTo(messageStructure.getTo());
		helper.setSubject(messageStructure.getSubject());
		helper.setSentDate(messageStructure.getSentDate());
		helper.setText(messageStructure.getText(), true);
		javaMailSender.send(mimeMessage);
	}

}
