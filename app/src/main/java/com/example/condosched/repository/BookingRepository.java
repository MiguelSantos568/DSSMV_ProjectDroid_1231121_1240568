package com.example.condosched.repository;

import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.Booking;
import com.example.condosched.domain.BookingValidator;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Resident;
import com.example.condosched.dto.AmenityDTO;
import com.example.condosched.dto.BookingDTO;
import com.example.condosched.dto.ResidentDTO;
import com.example.condosched.network.ApiClient;
import com.example.condosched.network.BookingApi;
import com.example.condosched.network.AmenityApi;
import com.example.condosched.network.ResidentApi;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Consulta regras e residentes na TheCondo; guarda e consulta marcações no Supabase. */
public class BookingRepository {
    private final BookingApi bookingApi;
    private final AmenityApi amenityApi;
    private final ResidentApi residentApi;
    private Call<?> activeRequest;
    private int version;

    public BookingRepository() {
        this(ApiClient.bookings(), ApiClient.amenities(), ApiClient.residents());
    }

    BookingRepository(BookingApi bookingApi, AmenityApi amenityApi, ResidentApi residentApi) {
        this.bookingApi = bookingApi;
        this.amenityApi = amenityApi;
        this.residentApi = residentApi;
    }

    public void loadResidents(long unitId, RepositoryCallback<List<Resident>> callback) {
        cancel();
        residents(unitId, version, callback);
    }

    private void residents(long unitId, int operation, RepositoryCallback<List<Resident>> callback) {
        if (unitId <= 0) {
            callback.onError(DataError.HOME_UNAVAILABLE);
            return;
        }
        execute(residentApi.getActiveResidents(), operation, new RepositoryCallback<List<ResidentDTO>>() {
            @Override
            public void onSuccess(List<ResidentDTO> data) {
                List<Resident> residents = new ArrayList<>();
                for (ResidentDTO dto : data) {
                    if (dto != null && dto.getId() > 0 && dto.isActive() && dto.getUnitId() == unitId) {
                        residents.add(BookingMapper.toDomain(dto));
                    }
                }
                callback.onSuccess(residents);
            }

            @Override
            public void onError(DataError error) {
                callback.onError(error);
            }
        });
    }

    public void create(long unitId, long amenityId, long residentId, LocalDateTime start,
                       LocalDateTime end, int people, String notes, RepositoryCallback<Booking> callback) {
        cancel();
        int operation = version;
        // 1. Consultar as regras atuais; 2. verificar residente; 3. verificar horário; 4. guardar.
        execute(amenityApi.getById(amenityId), operation, new RepositoryCallback<AmenityDTO>() {
            @Override
            public void onSuccess(AmenityDTO dto) {
                if (dto.getId() != amenityId) {
                    callback.onError(DataError.INVALID_RESPONSE);
                    return;
                }
                Amenity amenity = AmenityMapper.toDomain(dto, null);
                DataError error = BookingValidator.validate(amenity, start, end, people,
                        LocalDateTime.now(ZoneId.of("Europe/Lisbon")));
                if (error != null) {
                    callback.onError(error);
                    return;
                }
                checkResident(unitId, amenity, residentId, start, end, people, notes, operation, callback);
            }

            @Override
            public void onError(DataError error) {
                callback.onError(error);
            }
        });
    }

    private void checkResident(long unitId, Amenity amenity, long residentId, LocalDateTime start,
                               LocalDateTime end, int people, String notes, int operation,
                               RepositoryCallback<Booking> callback) {
        residents(unitId, operation, new RepositoryCallback<List<Resident>>() {
            @Override
            public void onSuccess(List<Resident> residents) {
                for (Resident resident : residents) {
                    if (resident.getId() == residentId) {
                        checkAvailability(unitId, amenity, residentId, start, end, people, notes, operation, callback);
                        return;
                    }
                }
                callback.onError(DataError.RESIDENT_REQUIRED);
            }

            @Override
            public void onError(DataError error) {
                callback.onError(error);
            }
        });
    }

    private void checkAvailability(long unitId, Amenity amenity, long residentId, LocalDateTime start,
                                   LocalDateTime end, int people, String notes, int operation,
                                   RepositoryCallback<Booking> callback) {
        execute(bookingApi.getForAmenity("eq." + amenity.getId(), "lt." + end, "gt." + start),
                operation, new RepositoryCallback<List<BookingDTO>>() {
            @Override
            public void onSuccess(List<BookingDTO> bookings) {
                List<Booking> existingBookings = new ArrayList<>();
                for (BookingDTO dto : bookings) {
                    if (dto == null) {
                        callback.onError(DataError.INVALID_RESPONSE);
                        return;
                    }
                    existingBookings.add(BookingMapper.toDomain(dto));
                }
                DataError error = BookingValidator.validateAvailability(amenity, start, end, people, existingBookings);
                if (error != null) {
                    callback.onError(error);
                    return;
                }
                BookingDTO request = new BookingDTO();
                request.setUnitId(unitId);
                request.setAmenityId(amenity.getId());
                request.setResidentId(residentId);
                request.setStartTime(start.toString());
                request.setEndTime(end.toString());
                request.setNumberOfGuests(people);
                request.setNotes(notes == null ? "" : notes.trim());
                request.setStatus("confirmed");
                request.setFee(amenity.getBookingFee());
                post(request, operation, callback);
            }

            @Override
            public void onError(DataError error) {
                callback.onError(error);
            }
        });
    }

    private void post(BookingDTO request, int operation, RepositoryCallback<Booking> callback) {
        execute(bookingApi.create(request), operation, new RepositoryCallback<List<BookingDTO>>() {
            @Override
            public void onSuccess(List<BookingDTO> data) {
                if (data.size() != 1 || data.get(0) == null) {
                    callback.onError(DataError.BOOKING_FAILED);
                    return;
                }
                BookingDTO result = data.get(0);
                if (!isValidBooking(result, request.getUnitId())
                        || result.getAmenityId() != request.getAmenityId()
                        || result.getResidentId() != request.getResidentId()
                        || result.getNumberOfGuests() != request.getNumberOfGuests()
                        || !"confirmed".equalsIgnoreCase(result.getStatus())) {
                    callback.onError(DataError.BOOKING_FAILED);
                    return;
                }
                try {
                    if (!LocalDateTime.parse(result.getStartTime()).equals(LocalDateTime.parse(request.getStartTime()))
                            || !LocalDateTime.parse(result.getEndTime()).equals(LocalDateTime.parse(request.getEndTime()))) {
                        callback.onError(DataError.BOOKING_FAILED);
                        return;
                    }
                } catch (DateTimeParseException | NullPointerException error) {
                    callback.onError(DataError.BOOKING_FAILED);
                    return;
                }
                callback.onSuccess(BookingMapper.toDomain(result));
            }

            @Override
            public void onError(DataError error) {
                // A lost POST response does not prove that the server did not save the booking.
                callback.onError(error == DataError.BOOKING_CONFLICT || error == DataError.BOOKING_SERVICE_UNAVAILABLE
                        ? error : DataError.BOOKING_FAILED);
            }
        });
    }

    public void deleteBooking(long unitId, long bookingId, RepositoryCallback<Long> callback) {
        cancel();
        if (unitId <= 0 || bookingId <= 0) {
            callback.onError(DataError.NOT_FOUND);
            return;
        }
        execute(bookingApi.deleteBooking("eq." + bookingId, "eq." + unitId), version,
                new RepositoryCallback<List<BookingDTO>>() {
            @Override
            public void onSuccess(List<BookingDTO> data) {
                if (data.isEmpty()) {
                    callback.onError(DataError.NOT_FOUND);
                    return;
                }
                BookingDTO result = data.get(0);
                if (data.size() != 1 || result == null || result.getId() == null
                        || result.getId() != bookingId || result.getUnitId() != unitId) {
                    callback.onError(DataError.BOOKING_DELETE_FAILED);
                    return;
                }
                callback.onSuccess(bookingId);
            }

            @Override
            public void onError(DataError error) {
                callback.onError(error == DataError.BOOKING_SERVICE_UNAVAILABLE
                        ? error : DataError.BOOKING_DELETE_FAILED);
            }
        });
    }

    public void loadForHome(long unitId, RepositoryCallback<List<Booking>> callback) {
        cancel();
        int operation = version;
        if (unitId <= 0) {
            callback.onError(DataError.HOME_UNAVAILABLE);
            return;
        }
        execute(bookingApi.getForHome("eq." + unitId), operation, new RepositoryCallback<List<BookingDTO>>() {
            @Override
            public void onSuccess(List<BookingDTO> data) {
                List<Booking> bookings = new ArrayList<>();
                for (BookingDTO dto : data) {
                    if (!isValidBooking(dto, unitId)) {
                        callback.onError(DataError.INVALID_RESPONSE);
                        return;
                    }
                    bookings.add(BookingMapper.toDomain(dto));
                }
                bookings.sort(Comparator.comparing(Booking::getStartTime).reversed());
                loadNames(bookings, operation, callback);
            }

            @Override
            public void onError(DataError error) {
                callback.onError(error);
            }
        });
    }

    private void loadNames(List<Booking> bookings, int operation, RepositoryCallback<List<Booking>> callback) {
        if (bookings.isEmpty()) {
            callback.onSuccess(bookings);
            return;
        }
        execute(amenityApi.getAll(), operation, new RepositoryCallback<List<AmenityDTO>>() {
            @Override
            public void onSuccess(List<AmenityDTO> amenities) {
                Map<Long, String> names = new HashMap<>();
                for (AmenityDTO amenity : amenities) {
                    if (amenity != null) names.put(amenity.getId(), amenity.getName());
                }
                for (Booking booking : bookings) booking.setAmenityName(names.get(booking.getAmenityId()));
                callback.onSuccess(bookings);
            }

            @Override
            public void onError(DataError error) {
                // The bookings remain useful even if names cannot be retrieved.
                callback.onSuccess(bookings);
            }
        });
    }

    private boolean isValidBooking(BookingDTO booking, long unitId) {
        if (booking == null || booking.getId() == null || booking.getId() <= 0
                || booking.getUnitId() != unitId || booking.getAmenityId() <= 0
                || booking.getResidentId() <= 0 || booking.getNumberOfGuests() <= 0
                || booking.getStatus() == null || booking.getStatus().trim().isEmpty()
                || booking.getStartTime() == null || booking.getEndTime() == null) return false;
        Double fee = booking.getFee();
        if (fee != null && (!Double.isFinite(fee) || fee < 0)) return false;
        try {
            return LocalDateTime.parse(booking.getEndTime()).isAfter(LocalDateTime.parse(booking.getStartTime()));
        } catch (DateTimeParseException error) {
            return false;
        }
    }

    private <T> void execute(Call<T> call, int operation, RepositoryCallback<T> callback) {
        activeRequest = call;
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> source, Response<T> response) {
                if (source.isCanceled() || operation != version) return;
                if (!response.isSuccessful()) {
                    boolean missingBookingService = response.code() == 404
                            && source.request().url().encodedPath().endsWith("/bookings");
                    DataError error = response.code() == 409 ? DataError.BOOKING_CONFLICT
                            : missingBookingService ? DataError.BOOKING_SERVICE_UNAVAILABLE : DataError.LOAD_FAILED;
                    callback.onError(error);
                } else if (response.body() == null) {
                    callback.onError(DataError.INVALID_RESPONSE);
                } else callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<T> source, Throwable error) {
                if (!source.isCanceled() && operation == version) callback.onError(DataError.LOAD_FAILED);
            }
        });
    }

    // Interrompe apenas o pedido HTTP ao sair do ecrã.
    public void cancel() {
        version++;
        if (activeRequest != null) activeRequest.cancel();
    }
}
