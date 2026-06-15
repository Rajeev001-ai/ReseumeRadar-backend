package com.resumeradar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration-ms}")
	private long jwtExpirationMs;

	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		Date issuedAt = new Date();
		Date expiresAt = new Date(issuedAt.getTime() + jwtExpirationMs);

		return Jwts.builder()
			.claims(extraClaims)
			.subject(userDetails.getUsername())
			.issuedAt(issuedAt)
			.expiration(expiresAt)
			.signWith(getSigningKey())
			.compact();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		String email = extractEmail(token);
		return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public String extractEmail(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = Jwts.parser()
			.setSigningKey(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
		return claimsResolver.apply(claims);
	}

	private Key getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
