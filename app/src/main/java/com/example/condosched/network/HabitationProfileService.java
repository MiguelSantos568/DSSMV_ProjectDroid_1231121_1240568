package com.example.condosched.network;

import com.example.condosched.domain.DTO.HabitationProfileDTO;
import com.example.condosched.domain.DTO.ProfileRequestDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class HabitationProfileService {
    private final HabitationProfileApi api;

    public HabitationProfileService() { this(ApiClient.profiles()); }
    HabitationProfileService(HabitationProfileApi api) { this.api = api; }
    public interface ProfileCallback {
        // null means the lookup succeeded but this home does not have a profile yet.
        void onSuccess(HabitationProfileDTO profile);
        void onError(String message);
    }

    public void find(long unitId, ProfileCallback callback) {
        ProfileRequestDTO request = new ProfileRequestDTO();
        request.setUnitId(unitId);
        execute(api.find(request), unitId, true, callback);
    }

    public void create(long unitId, String name, String email, ProfileCallback callback) {
        ProfileRequestDTO request = new ProfileRequestDTO();
        request.setUnitId(unitId);
        request.setFullName(name);
        request.setEmail(email);
        execute(api.create(request), unitId, false, callback);
    }

    private void execute(Call<List<HabitationProfileDTO>> call, long unitId,
                         boolean allowMissing, ProfileCallback callback) {
        call.enqueue(new Callback<List<HabitationProfileDTO>>() {
            @Override public void onResponse(Call<List<HabitationProfileDTO>> request,
                                             Response<List<HabitationProfileDTO>> response) {
                if (!response.isSuccessful()) {
                    callback.onError(response.code() == 404
                            ? "O serviço de perfis está indisponível. Contacta o responsável pela aplicação."
                            : "Não foi possível aceder ao perfil no Supabase (HTTP " + response.code() + "). Tenta novamente.");
                    return;
                }
                List<HabitationProfileDTO> profiles = response.body();
                if (profiles == null) {
                    callback.onError("Resposta inválida do Supabase. Tenta novamente.");
                    return;
                }
                if (profiles.isEmpty() && allowMissing) {
                    callback.onSuccess(null);
                    return;
                }
                if (profiles.size() != 1 || !isValid(profiles.get(0), unitId)) {
                    callback.onError("O perfil devolvido pelo Supabase é inválido. Tenta novamente.");
                    return;
                }
                callback.onSuccess(profiles.get(0));
            }
            @Override public void onFailure(Call<List<HabitationProfileDTO>> request, Throwable error) {
                callback.onError("Não foi possível ligar ao Supabase. Verifica a ligação e tenta novamente.");
            }
        });
    }

    private boolean isValid(HabitationProfileDTO profile, long unitId) {
        return profile != null && profile.getUnitId() == unitId
                && profile.getFullName() != null && !profile.getFullName().trim().isEmpty()
                && profile.getEmail() != null && !profile.getEmail().trim().isEmpty();
    }
}
