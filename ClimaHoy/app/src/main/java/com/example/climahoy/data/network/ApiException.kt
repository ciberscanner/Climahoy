package com.example.climahoy.data.network

class ApiException(val code: Int, message: String, val error: ErrorAPI?) : Exception("Error $code: $message")
