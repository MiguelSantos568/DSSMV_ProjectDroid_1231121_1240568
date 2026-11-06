package com.example.condosched.repository;

import com.example.condosched.domain.DataError;

public interface RepositoryCallback<T> {
    void onSuccess(T result);

    void onError(DataError error);
}
