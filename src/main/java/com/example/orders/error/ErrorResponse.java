package com.example.orders.error;

import java.util.List;

public record ErrorResponse(int status, String message, List<String> error ) {
}
