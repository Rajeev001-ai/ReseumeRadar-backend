package com.resumeradar.exception;

import com.resumeradar.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateEmail(
		DuplicateEmailException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidLoginException.class)
	public ResponseEntity<ErrorResponse> handleInvalidLogin(
		InvalidLoginException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
	}

	@ExceptionHandler({UserNotFoundException.class, UsernameNotFoundException.class})
	public ResponseEntity<ErrorResponse> handleUserNotFound(
		RuntimeException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(FileUploadException.class)
	public ResponseEntity<ErrorResponse> handleFileUpload(
		FileUploadException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxUploadSize(
		MaxUploadSizeExceededException exception,
		HttpServletRequest request
	) {
		return buildError(
			HttpStatus.PAYLOAD_TOO_LARGE,
			"PDF resume file size exceeds the allowed limit",
			request
		);
	}

	@ExceptionHandler(AnalysisNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAnalysisNotFound(
		AnalysisNotFoundException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
		UnauthorizedAccessException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.FORBIDDEN, exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		List<String> details = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();
		HttpStatus status = HttpStatus.BAD_REQUEST;

		return ResponseEntity
			.status(status)
			.body(ErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				"Validation failed",
				request.getRequestURI(),
				details
			));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(
		RuntimeException exception,
		HttpServletRequest request
	) {
		return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request);
	}

	private ResponseEntity<ErrorResponse> buildError(
		HttpStatus status,
		String message,
		HttpServletRequest request
	) {
		return ResponseEntity
			.status(status)
			.body(ErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
			));
	}
}
