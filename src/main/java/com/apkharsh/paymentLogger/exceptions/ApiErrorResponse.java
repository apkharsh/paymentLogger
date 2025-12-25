package com.apkharsh.paymentLogger.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;
    
    // Optional: Add more fields for detailed errors
    private String path;
    private List<String> errors; // For validation errors
}
