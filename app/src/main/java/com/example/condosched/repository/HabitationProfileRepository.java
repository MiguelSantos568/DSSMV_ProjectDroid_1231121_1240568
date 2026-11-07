package com.example.condosched.repository;

import com.example.condosched.network.ApiClient;
import com.example.condosched.network.HabitationProfileApi;
import com.example.condosched.domain.HabitationProfile;
import com.example.condosched.domain.DataError;

import com.example.condosched.dto.HabitationProfileDTO;
import com.example.condosched.domain.ProfileContactValidator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitationProfileRepository {
    private final HabitationProfileApi api;
    private Call<?> activeRequest;
    private int version;

    public HabitationProfileRepository() {
        this(ApiClient.profiles());
    }

    HabitationProfileRepository(HabitationProfileApi api) {
        this.api = api;
    }

    public void find(long unitId, RepositoryCallback<HabitationProfile> callback) {
        if (unitId <= 0) {
            callback.onError(DataError.HOME_UNAVAILABLE);
            return;
        }
        execute(api.find("eq." + unitId), unitId, true, callback);
    }

    public void create(long unitId, String name, String email, RepositoryCallback<HabitationProfile> callback) {
        DataError error = ProfileContactValidator.validate(name, email);
        if (unitId <= 0 || error != null) {
            callback.onError(unitId <= 0 ? DataError.HOME_UNAVAILABLE : error);
            return;
        }
        HabitationProfileDTO request = new HabitationProfileDTO();
        request.setUnitId(unitId);
        request.setFullName(name.trim());
        request.setEmail(email.trim());
        execute(api.create(request), unitId, false, callback);
    }

    private void execute(Call<List<HabitationProfileDTO>> call, long unitId,
                         boolean allowMissing, RepositoryCallback<HabitationProfile> callback) {
        cancel();
        final int current = version;
        activeRequest = call;
        call.enqueue(new Callback<List<HabitationProfileDTO>>() {
            @Override
            public void onResponse(Call<List<HabitationProfileDTO>> request,
                                   Response<List<HabitationProfileDTO>> response) {
                if (request.isCanceled() || current != version) return;
                if (!response.isSuccessful()) {
                    // Outra instalação pode ter registado a mesma casa primeiro.
                    if (response.code() == 409 && !allowMissing) {
                        find(unitId, new RepositoryCallback<HabitationProfile>() {
                            @Override
                            public void onSuccess(HabitationProfile profile) {
                                if (profile == null) callback.onError(DataError.INVALID_RESPONSE);
                                else callback.onSuccess(profile);
                            }

                            @Override
                            public void onError(DataError error) {
                                callback.onError(error);
                            }
                        });
                        return;
                    }
                    callback.onError(response.code() == 404 ? DataError.SERVICE_UNAVAILABLE : DataError.LOAD_FAILED);
                    return;
                }
                List<HabitationProfileDTO> profiles = response.body();
                if (profiles == null) {
                    callback.onError(DataError.INVALID_RESPONSE);
                    return;
                }
                if (profiles.isEmpty() && allowMissing) {
                    callback.onSuccess(null);
                    return;
                }
                if (profiles.size() != 1 || !isValid(profiles.get(0), unitId)) {
                    callback.onError(DataError.INVALID_RESPONSE);
                    return;
                }
                HabitationProfileDTO profile = profiles.get(0);
                callback.onSuccess(new HabitationProfile(profile.getUnitId(),
                        profile.getFullName(), profile.getEmail()));
            }

            @Override
            public void onFailure(Call<List<HabitationProfileDTO>> request, Throwable error) {
                if (!request.isCanceled() && current == version) callback.onError(DataError.LOAD_FAILED);
            }
        });
    }

    private boolean isValid(HabitationProfileDTO profile, long unitId) {
        return profile != null && profile.getUnitId() == unitId
                && profile.getFullName() != null && !profile.getFullName().trim().isEmpty()
                && profile.getEmail() != null && !profile.getEmail().trim().isEmpty();
    }

    public void cancel() {
        version++;
        if (activeRequest != null) activeRequest.cancel();
    }
}
