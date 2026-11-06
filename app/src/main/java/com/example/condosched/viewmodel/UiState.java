package com.example.condosched.viewmodel;

import com.example.condosched.domain.DataError;

/**
 * Estado comum aos ecrãs: dados, fase da operação e eventual erro.
 */
public final class UiState<T> {
    public enum Status {IDLE, LOADING, SUCCESS, ERROR}

    private final Status status;
    private final T data;
    private final DataError error;

    private UiState(Status status, T data, DataError error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> UiState<T> idle() {
        return new UiState<>(Status.IDLE, null, null);
    }

    public static <T> UiState<T> loading(T data) {
        return new UiState<>(Status.LOADING, data, null);
    }

    public static <T> UiState<T> success(T data) {
        return new UiState<>(Status.SUCCESS, data, null);
    }

    public static <T> UiState<T> failure(DataError error, T cached) {
        return new UiState<>(Status.ERROR, cached, error);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public DataError getError() {
        return error;
    }
}
