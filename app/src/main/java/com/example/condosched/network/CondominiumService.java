package com.example.condosched.network;

import com.example.condosched.domain.DTO.CondominiumDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CondominiumService {
    public interface CondominiumCallback {
        void onSuccess(List<CondominiumDTO> condominiums);
        void onError(String message);
    }

    public void getCondominiums(CondominiumCallback callback) {
        // enqueue makes the HTTP request asynchronously. On Android, the callback runs on the UI thread.
        ApiClient.condominiums().getCondominiums().enqueue(new Callback<List<CondominiumDTO>>() {
            @Override public void onResponse(Call<List<CondominiumDTO>> call,
                                             Response<List<CondominiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro ao consultar habitações (HTTP " + response.code() + ").");
                }
            }
            @Override public void onFailure(Call<List<CondominiumDTO>> call, Throwable error) {
                callback.onError("Não foi possível consultar as habitações. Verifica a ligação e tenta novamente.");
            }
        });
    }
}