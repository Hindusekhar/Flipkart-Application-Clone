package com.jsp.fc.serviceimpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.catalina.authenticator.SpnegoAuthenticator.AuthenticateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jsp.fc.cache.CacheStore;
import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.Customer;
import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.entity.Seller;
import com.jsp.fc.entity.User;
import com.jsp.fc.exception.ExpiredException;
import com.jsp.fc.exception.InvalidOtpException;
import com.jsp.fc.exception.NotFoundException;
import com.jsp.fc.exception.UserLoggedOutException;
import com.jsp.fc.exception.UserNotLoggedException;
import com.jsp.fc.repository.AccessTokenRepo;
import com.jsp.fc.repository.CustomerRepository;
import com.jsp.fc.repository.RefreshTokenRepo;
import com.jsp.fc.repository.SellerRepository;
import com.jsp.fc.repository.UserRepository;
import com.jsp.fc.requestdto.AuthRequest;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.AuthResponse;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.security.JwtService;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.util.CookieManager;
import com.jsp.fc.util.MessageStructure;
import com.jsp.fc.util.ResponseStructure;
import com.jsp.fc.util.SimpleStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.websocket.Encoder.Text;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
	private SellerRepository sellerRepository;
	private CustomerRepository customerRepository;
	private ResponseStructure<UserResponse> responseStructure;
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private CacheStore<User> userCacheStore;
	private JavaMailSender javaMailSender;
	private CacheStore<String> otpCacheStore;
	private AuthenticationManager authenticationManager;
	private CookieManager cookieManager;
	private JwtService jwtService;
	private RefreshTokenRepo refreshTokenRepo;
	private AccessTokenRepo accessTokenRepo;
	private SimpleStructure simpleStructure;

	@Value("${myapp.access.expiry}")
	private int accessExpiryInSeconds;

	@Value("${myapp.refresh.expiry}")
	private int refreshExpiryInSeconds;

	public AuthServiceImpl(SellerRepository sellerRepository, CustomerRepository customerRepository,
			ResponseStructure<UserResponse> responseStructure, UserRepository userRepository,
			PasswordEncoder passwordEncoder, CacheStore<User> userCacheStore, JavaMailSender javaMailSender,
			CacheStore<String> otpCacheStore, AuthenticationManager authenticationManager, CookieManager cookieManager,
			JwtService jwtService, RefreshTokenRepo refreshTokenRepo, AccessTokenRepo accessTokenRepo,
			SimpleStructure simpleStructure) {

		this.sellerRepository = sellerRepository;
		this.customerRepository = customerRepository;
		this.responseStructure = responseStructure;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userCacheStore = userCacheStore;
		this.javaMailSender = javaMailSender;
		this.otpCacheStore = otpCacheStore;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.refreshTokenRepo = refreshTokenRepo;
		this.accessTokenRepo = accessTokenRepo;
		this.simpleStructure = simpleStructure;
	}

	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		if (userRepository.existsByUserEmail(userRequest.getUserEmail()))
			throw new RuntimeException("user already preset");

		String otp = generateOtp();
		User user = mapToUser(userRequest);
		log.info(otp);

		otpCacheStore.add(user.getUserEmail(), otp);
		userCacheStore.add(user.getUserEmail(), user);

		try {
			sendOtpToMail(user, otp);
		} catch (MessagingException e) {

			e.printStackTrace();
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
		log.info("" + user.isEmailVerified());

		return new ResponseEntity<ResponseStructure<UserResponse>>(
				responseStructure.setStatus(HttpStatus.CREATED.value()).setMessage("succesfully registerd with us")
						.setData(mapToUserResponse(user)),
				HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest,
			HttpServletResponse httpServletResponse,String accessToken, String refereshToken) {
		
		if(accessToken!=null && refereshToken!=null)throw new UserLoggedOutException("user logged out ");
		String userName = authRequest.getUserEmail().split("@")[0];
		UsernamePasswordAuthenticationToken passwordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				userName, authRequest.getPassword());
		Authentication authentication = authenticationManager.authenticate(passwordAuthenticationToken);
		if (!authentication.isAuthenticated())
			throw new UsernameNotFoundException(" failed to authenticate the user");
		else {
			// generating the cookies and authresoponse and returning to the client
			return userRepository.findByUserName(userName).map(user -> {

				grantAccess(httpServletResponse, user);
				ResponseStructure<AuthResponse> responseStructure = new ResponseStructure<>();
				return new ResponseEntity<ResponseStructure<AuthResponse>>(responseStructure
						.setStatus(HttpStatus.OK.value()).setMessage("login sucessful")
						.setData(AuthResponse.builder().userId(user.getUserId()).userName(user.getUserName())
								.role(user.getUserRole().name()).isAuthenticated(true)
								.accessExpiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
								.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds)).build()),
						HttpStatus.OK);
			}).get();
		}

	}

	@Override
	public ResponseEntity<SimpleStructure> logOut(String accessToken, String refereshToken,
			HttpServletResponse httpServletResponse) {
		log.info(refereshToken);
		log.info(accessToken);
		if (accessToken == null && refereshToken == null)
			throw new UserNotLoggedException("user not logged in");

		accessTokenRepo.findByToken(accessToken).ifPresent(token -> {
			token.setBlocked(true);
			accessTokenRepo.save(token);
		});
		accessTokenRepo.findByToken(refereshToken).ifPresent(token -> {
			token.setBlocked(true);
			accessTokenRepo.save(token);
		});

		httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("rt", "")));

		simpleStructure.setMessage("succesfully ogged out");
		simpleStructure.setStatus(HttpStatus.OK.value());
		return new ResponseEntity<SimpleStructure>(simpleStructure, HttpStatus.OK);

	}

	//
	// @Override
	// public ResponseEntity<ResponseStructure<String>> logOut(HttpServletResponse
	// response, HttpServletRequest request) {
	//
	// Cookie[] cookies = request.getCookies();
	// for (Cookie cookie : cookies) {
	// if (cookie.getName().equals("rt"))
	// rt = cookie.getValue();
	// if (cookie.getName().equals("at"))
	// at = cookie.getValue();
	//
	// accessTokenRepo.findByToken(at).ifPresent(accessToken -> {
	// accessToken.setBlocked(true);
	// accessTokenRepo.save(accessToken);
	// });
	//
	// response.addCookie(cookieManager.invalidate(new Cookie("at", "")));
	// response.addCookie(cookieManager.invalidate(new Cookie("rt", "")));
	//
	// }
	//
	// ResponseStructure<String> responseStructure = new ResponseStructure<>();
	//
	// return new ResponseEntity<ResponseStructure<String>>(
	// responseStructure.setMessage("logged in
	// sucessful").setStatus(HttpStatus.OK.value()),HttpStatus.OK);
	// }

	private void grantAccess(HttpServletResponse httpServletResponse, User user) {
		//generating access and refresh tokens
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());
		
		//adding access and refresh tokens cookies to the response
		httpServletResponse.addCookie(cookieManager.configure(new Cookie("at", accessToken), accessExpiryInSeconds));
		httpServletResponse.addCookie(cookieManager.configure(new Cookie("rt", refreshToken), accessExpiryInSeconds));

		
		 //saving the access and refresh cookie into database
		accessTokenRepo.save(AccessToken.builder().token(accessToken).isBlocked(false).user(user)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds)).build());
		refreshTokenRepo.save(RefreshToken.builder().token(refreshToken).isBlocked(false).user(user)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds)).build());

	}

	private UserResponse mapToUserResponse(User user) {
		return UserResponse.builder().userId(user.getUserId()).userName(user.getUserName())
				.userEmail(user.getUserEmail()).userRole(user.getUserRole()).isEmailVerified(user.isEmailVerified())
				.isDeleted(user.isDeleted()).build();
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

	@Override
	public ResponseEntity<SimpleStructure> revokeAcess(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse) {

		
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("--------------------------------------------------"+userName);
		
		if (userName == null)
			throw new NotFoundException("user name is null");
		 userRepository.findByUserName(userName).ifPresent(user->{
			 blockAccessTokens(accessTokenRepo.findAllByUserAndIsBlockedAndAndTokenNot(user, false, accessToken));

				blockRefreshTokens(refreshTokenRepo.findAllByUserAndIsBlockedAndAndTokenNot(user, false, refreshToken));

		 });//orElseThrow(() -> new NotFoundException("user not found"));

		
		httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("at", "")));
		httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("rt", "")));

		simpleStructure.setMessage("all are revoked");
		simpleStructure.setStatus(HttpStatus.OK.value());
		return new ResponseEntity<SimpleStructure>(simpleStructure, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SimpleStructure> revokeOtherAcess(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse) {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		if (userName == null)
			throw new NotFoundException("user name is null");
		userRepository.findByUserName(userName).ifPresent(user->{
			
			blockAccessTokens(accessTokenRepo.findAllByUserAndIsBlockedAndAndTokenNot(user, false, accessToken));

			blockRefreshTokens(refreshTokenRepo.findAllByUserAndIsBlockedAndAndTokenNot(user, false, refreshToken));
			
			httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("at", "")));
			httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("rt", "")));


		});

		
		
		simpleStructure.setMessage("all are revoked");
		simpleStructure.setStatus(HttpStatus.OK.value());
		return new ResponseEntity<SimpleStructure>(simpleStructure, HttpStatus.OK);
	}

	private void blockAccessTokens(List<AccessToken> accessTokens) {
		for (AccessToken accessToken : accessTokens) {
			accessToken.setBlocked(true);
			accessTokenRepo.save(accessToken);
		}
	}

	private void blockRefreshTokens(List<RefreshToken> refreshTokens) {
		for (RefreshToken refreshToken : refreshTokens) {
			refreshToken.setBlocked(true);
			refreshTokenRepo.save(refreshToken);
		}
	}

	@Override
	public ResponseEntity<SimpleStructure> refreshToken(String accessToken, String refreshToken,
			HttpServletResponse httpServletResponse) {
		
		
			accessTokenRepo.findByToken(accessToken)
			.ifPresent(at->{at.setBlocked(true);
			accessTokenRepo.save(at);
			});
						
		
		if(refreshToken==null) throw new UserLoggedOutException("user is logged out");
		refreshTokenRepo.findByToken(accessToken)
		.ifPresent(rt->{
			grantAccess(httpServletResponse,rt.getUser());
			revokeOtherAcess(accessToken, refreshToken, httpServletResponse);	
			
			
			httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("at", "")));
			httpServletResponse.addCookie(cookieManager.invalidate(new Cookie("rt", "")));

			}); 


		
		simpleStructure.setMessage("tokens are refreshed");		
		simpleStructure.setStatus(HttpStatus.FOUND.value());

		return new ResponseEntity<SimpleStructure>(simpleStructure,HttpStatus.FOUND);

	}


}
