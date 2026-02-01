package com.example.climahoy.utils

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T, val message: String) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    data class Warning(val message: String) : UiState<Nothing>()
}