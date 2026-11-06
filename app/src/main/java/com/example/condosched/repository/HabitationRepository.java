package com.example.condosched.repository;

import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Habitation;
import com.example.condosched.dto.CondominiumDTO;
import com.example.condosched.network.ApiClient;
import com.example.condosched.network.CondominiumApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitationRepository {
    private final CondominiumApi api;
    private Call<?> request;
    private int version;

    public HabitationRepository() {
        this(ApiClient.condominiums());
    }

    HabitationRepository(CondominiumApi api) {
        this.api = api;
    }

    public void find(long unitId, RepositoryCallback<Habitation> callback) {
        cancel();
        int current = version;
        if (unitId <= 0) {
            callback.onError(DataError.HOME_UNAVAILABLE);
            return;
        }
        Call<List<CondominiumDTO>> call = api.getCondominiums();
        request = call;
        call.enqueue(new Callback<List<CondominiumDTO>>() {
            @Override
            public void onResponse(Call<List<CondominiumDTO>> source, Response<List<CondominiumDTO>> response) {
                if (source.isCanceled() || current != version) return;
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(response.isSuccessful() ? DataError.INVALID_RESPONSE : DataError.LOAD_FAILED);
                    return;
                }
                Habitation home = HabitationMapper.findIn(response.body(), unitId);
                if (home == null) callback.onError(DataError.HOME_UNAVAILABLE);
                else callback.onSuccess(home);
            }

            @Override
            public void onFailure(Call<List<CondominiumDTO>> source, Throwable error) {
                if (!source.isCanceled() && current == version) callback.onError(DataError.LOAD_FAILED);
            }
        });
    }

    public void cancel() {
        version++;
        if (request != null) request.cancel();
    }
}