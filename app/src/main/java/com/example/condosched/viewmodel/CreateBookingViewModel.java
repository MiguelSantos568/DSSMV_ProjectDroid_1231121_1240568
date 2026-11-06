package com.example.condosched.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.BookingValidator;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Resident;
import com.example.condosched.repository.BookingRepository;
import com.example.condosched.repository.RepositoryCallback;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

public class CreateBookingViewModel extends ViewModel {
    private final BookingRepository repository;
    private final MutableLiveData<UiState<List<Resident>>> residents = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Booking>> result = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<LocalDate> date = new MutableLiveData<>();
    private final MutableLiveData<LocalTime> start = new MutableLiveData<>();
    private final MutableLiveData<LocalTime> end = new MutableLiveData<>();
    private long unitId;
    private Amenity amenity;

    public CreateBookingViewModel() {
        this(new BookingRepository());
    }

    CreateBookingViewModel(BookingRepository repository) {
        this.repository = repository;
    }

    public LiveData<UiState<List<Resident>>> getResidents() {
        return residents;
    }

    public LiveData<UiState<Booking>> getResult() {
        return result;
    }

    public LiveData<LocalDate> getDate() {
        return date;
    }

    public LiveData<LocalTime> getStart() {
        return start;
    }

    public LiveData<LocalTime> getEnd() {
        return end;
    }

    public void load(long homeId, Amenity selected) {
        // A rotação reutiliza o ViewModel: não repor o formulário nem repetir pedidos.
        if (amenity != null) return;
        unitId = homeId;
        amenity = selected;
        date.setValue(LocalDate.now(ZoneId.of("Europe/Lisbon")).plusDays(1));
        start.setValue(selected.getOpeningTime() == null ? LocalTime.of(9, 0) : selected.getOpeningTime());
        end.setValue(start.getValue().plusHours(1));
        retryResidents();
    }

    public void retryResidents() {
        if (isSubmitting()) return;
        residents.setValue(UiState.loading(Collections.emptyList()));
        repository.loadResidents(unitId, new RepositoryCallback<List<Resident>>() {
            @Override
            public void onSuccess(List<Resident> data) {
                residents.setValue(data.isEmpty() ? UiState.failure(DataError.NO_RESIDENTS, data)
                        : UiState.success(data));
            }

            @Override
            public void onError(DataError error) {
                residents.setValue(UiState.failure(error, Collections.emptyList()));
            }
        });
    }

    public void setDate(LocalDate value) {
        if (canEdit()) date.setValue(value);
    }

    public void setStart(LocalTime value) {
        if (canEdit()) start.setValue(value);
    }

    public void setEnd(LocalTime value) {
        if (canEdit()) end.setValue(value);
    }

    private boolean canEdit() {
        return !isSubmitting() && result.getValue().getStatus() != UiState.Status.SUCCESS;
    }

    private boolean isSubmitting() {
        return result.getValue().getStatus() == UiState.Status.LOADING;
    }

    public void submit(long residentId, String peopleText, String notes) {
        if (!canEdit() || amenity == null) return;
        if (!hasResident(residentId)) {
            result.setValue(UiState.failure(DataError.RESIDENT_REQUIRED, null));
            return;
        }
        final int people;
        try {
            people = Integer.parseInt(peopleText.trim());
        } catch (NumberFormatException | NullPointerException error) {
            result.setValue(UiState.failure(DataError.INVALID_GUEST_COUNT, null));
            return;
        }
        LocalDateTime startDateTime = date.getValue().atTime(start.getValue());
        LocalDateTime endDateTime = date.getValue().atTime(end.getValue());
        if (end.getValue().equals(LocalTime.MIDNIGHT)) endDateTime = endDateTime.plusDays(1);
        DataError error = BookingValidator.validate(amenity, startDateTime, endDateTime, people,
                LocalDateTime.now(ZoneId.of("Europe/Lisbon")));
        if (error != null) {
            result.setValue(UiState.failure(error, null));
            return;
        }
        result.setValue(UiState.loading(null));
        repository.create(unitId, amenity.getId(), residentId, startDateTime, endDateTime, people, notes,
                new RepositoryCallback<Booking>() {
                    @Override
                    public void onSuccess(Booking booking) {
                        result.setValue(UiState.success(booking));
                    }

                    @Override
                    public void onError(DataError error) {
                        result.setValue(UiState.failure(error, null));
                    }
                });
    }

    private boolean hasResident(long residentId) {
        List<Resident> available = residents.getValue().getData();
        if (available == null) return false;
        for (Resident resident : available) {
            if (resident.getId() == residentId) return true;
        }
        return false;
    }

    @Override
    protected void onCleared() {
        repository.cancel();
    }
}
