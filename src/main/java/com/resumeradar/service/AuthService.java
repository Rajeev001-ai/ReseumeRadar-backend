package com.resumeradar.service;

import com.resumeradar.dto.AuthResponse;
import com.resumeradar.dto.LoginRequest;
import com.resumeradar.dto.RegisterRequest;
import com.resumeradar.entity.Role;
import com.resumeradar.entity.User;
import com.resumeradar.exception.DuplicateEmailException;
import com.resumeradar.exception.InvalidLoginException;
import com.resumeradar.exception.UserNotFoundException;
import com.resumeradar.repository.UserRepository;
import com.resumeradar.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	private final PasswordEncoder passwordEncoder;

	private final UserRepository userRepository;

	public AuthService(
		AuthenticationManager authenticationManager,
		JwtService jwtService,
		PasswordEncoder passwordEncoder,
		UserRepository userRepository
	) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateEmailException("Email is already registered");
		}

		User user = User.builder()
			.fullName(request.getFullName())
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.role(Role.USER)
			.build();

		User savedUser = userRepository.save(user);
		UserDetails userDetails = buildUserDetails(savedUser);
		String token = jwtService.generateToken(userDetails);

		return toAuthResponse(token, savedUser);
	}

	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
			);
		} catch (BadCredentialsException exception) {
			throw new InvalidLoginException("Invalid email or password");
		} catch (AuthenticationException exception) {
			throw new InvalidLoginException("Invalid email or password");
		}

		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new UserNotFoundException("User not found"));
		UserDetails userDetails = buildUserDetails(user);
		String token = jwtService.generateToken(userDetails);

		return toAuthResponse(token, user);
	}

	private UserDetails buildUserDetails(User user) {
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getEmail())
			.password(user.getPassword())
			.authorities("ROLE_" + user.getRole().name())
			.build();
	}

	private AuthResponse toAuthResponse(String token, User user) {
		return AuthResponse.builder()
			.token(token)
			.fullName(user.getFullName())
			.email(user.getEmail())
			.role(user.getRole())
			.build();
	}
}
