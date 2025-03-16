package com.roomfinder.exceptions;

import com.roomfinder.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse response = new ApiResponse(false, "Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UserNotFoundException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(ValidationException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ApiResponse> handleInvalidBooking(InvalidBookingException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiResponse> handleBookingNotFound(BookingNotFoundException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        ApiResponse response = new ApiResponse(false, "An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiResponse> handleRoomNotFound(RoomNotFoundException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    @ExceptionHandler(ErrorResponse.class)
    public ResponseEntity<ApiResponse> handleErrorResponse(ErrorResponse ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


}