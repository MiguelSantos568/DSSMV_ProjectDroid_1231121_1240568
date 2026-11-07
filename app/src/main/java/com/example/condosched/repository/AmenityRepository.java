package com.example.condosched.repository;

import com.example.condosched.dto.AmenityDTO;
import com.example.condosched.domain.Amenity;
import com.example.condosched.network.ApiClient;
import com.example.condosched.network.AmenityApi;
import com.example.condosched.domain.DataError;
import com.example.condosched.dto.BuildingDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AmenityRepository {
    private final AmenityApi api;
    private Call<?> activeRequest;
    private int requestVersion;

    public AmenityRepository() {
        this(ApiClient.amenities());
    }

    AmenityRepository(AmenityApi api) {
        this.api = api;
    }

    // Uma nova consulta invalida respostas atrasadas da consulta anterior.
    public void getByCondominium(long condominiumId, RepositoryCallback<List<Amenity>> callback) {
        cancel();
        final int version = requestVersion;
        if (condominiumId <= 0) {
            callback.onError(DataError.HOME_UNAVAILABLE);
            return;
        }
        Call<List<BuildingDTO>> buildings = api.getBuildingsByCondominium(condominiumId);
        activeRequest = buildings;
        buildings.enqueue(new Callback<List<BuildingDTO>>() {
            @Override
            public void onResponse(Call<List<BuildingDTO>> call, Response<List<BuildingDTO>> response) {
                if (call.isCanceled() || version != requestVersion) return;
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(response.isSuccessful() ? DataError.INVALID_RESPONSE : DataError.LOAD_FAILED);
                    return;
                }
                Map<Long, String> names = new LinkedHashMap<>();
                for (BuildingDTO building : response.body()) {
                    if (building != null && building.getCondominiumId() == condominiumId) {
                        names.put(building.getId(), building.getName());
                    }
                }
                loadAmenities(condominiumId, names, version, callback);
            }

            @Override
            public void onFailure(Call<List<BuildingDTO>> call, Throwable error) {
                if (!call.isCanceled() && version == requestVersion) callback.onError(DataError.LOAD_FAILED);
            }
        });
    }

    private void loadAmenities(long condominiumId, Map<Long, String> names, int version, RepositoryCallback<List<Amenity>> callback) {
        Call<List<AmenityDTO>> call = api.getByCondominium(condominiumId);
        activeRequest = call;
        call.enqueue(new Callback<List<AmenityDTO>>() {
            @Override
            public void onResponse(Call<List<AmenityDTO>> request, Response<List<AmenityDTO>> response) {
                if (request.isCanceled() || version != requestVersion) return;
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(response.isSuccessful() ? DataError.INVALID_RESPONSE : DataError.LOAD_FAILED);
                    return;
                }
                List<Amenity> available = new ArrayList<>();
                for (AmenityDTO amenity : response.body()) {
                    // Include all buildings of this condominium, and exclude other condominiums.
                    if (amenity != null && names.containsKey(amenity.getBuildingId()) && amenity.isActive()) {
                        available.add(AmenityMapper.toDomain(amenity, names.get(amenity.getBuildingId())));
                    }
                }
                callback.onSuccess(available);
            }

            @Override
            public void onFailure(Call<List<AmenityDTO>> request, Throwable error) {
                if (!request.isCanceled() && version == requestVersion) callback.onError(DataError.LOAD_FAILED);
            }
        });
    }

    public void cancel() {
        requestVersion++;
        if (activeRequest != null) activeRequest.cancel();
    }
}
