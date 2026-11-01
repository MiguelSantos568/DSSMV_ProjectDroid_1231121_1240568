package com.example.condosched.network;

import com.example.condosched.domain.DTO.AmenityDTO;
import com.example.condosched.domain.DTO.BuildingDTO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class AmenityService {
    private final AmenityApi api;
    private Call<?> activeRequest;

    public AmenityService() { this(ApiClient.amenities()); }
    AmenityService(AmenityApi api) { this.api = api; }

    public interface AmenityCallback {
        void onSuccess(List<AmenityDTO> amenities, Map<Long, String> buildingNames);
        void onError();
    }

    public void getByCondominium(long condominiumId, AmenityCallback callback) {
        if (condominiumId <= 0) {
            callback.onError();
            return;
        }
        Call<List<BuildingDTO>> buildings = api.getBuildingsByCondominium(condominiumId);
        activeRequest = buildings;
        buildings.enqueue(new Callback<List<BuildingDTO>>() {
            @Override public void onResponse(Call<List<BuildingDTO>> call, Response<List<BuildingDTO>> response) {
                if (call.isCanceled()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError();
                    return;
                }
                Map<Long, String> names = new LinkedHashMap<>();
                for (BuildingDTO building : response.body()) {
                    if (building != null && building.getCondominiumId() == condominiumId) {
                        names.put(building.getId(), building.getName());
                    }
                }
                loadAmenities(condominiumId, names, callback);
            }
            @Override public void onFailure(Call<List<BuildingDTO>> call, Throwable error) {
                if (!call.isCanceled()) callback.onError();
            }
        });
    }

    private void loadAmenities(long condominiumId, Map<Long, String> names, AmenityCallback callback) {
        Call<List<AmenityDTO>> call = api.getByCondominium(condominiumId);
        activeRequest = call;
        call.enqueue(new Callback<List<AmenityDTO>>() {
            @Override public void onResponse(Call<List<AmenityDTO>> request, Response<List<AmenityDTO>> response) {
                if (request.isCanceled()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError();
                    return;
                }
                List<AmenityDTO> available = new ArrayList<>();
                for (AmenityDTO amenity : response.body()) {
                    // Include all buildings of this condominium, and exclude other condominiums.
                    if (amenity != null && names.containsKey(amenity.getBuildingId()) && amenity.isActive()) {
                        available.add(amenity);
                    }
                }
                callback.onSuccess(available, names);
            }
            @Override public void onFailure(Call<List<AmenityDTO>> request, Throwable error) {
                if (!request.isCanceled()) callback.onError();
            }
        });
    }

    public void cancel() {
        if (activeRequest != null) activeRequest.cancel();
    }
}