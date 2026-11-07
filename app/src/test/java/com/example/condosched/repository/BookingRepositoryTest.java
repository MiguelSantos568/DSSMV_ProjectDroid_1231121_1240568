package com.example.condosched.repository;

import com.example.condosched.domain.Booking;
import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Resident;
import com.example.condosched.network.AmenityApi;
import com.example.condosched.network.BookingApi;
import com.example.condosched.network.ResidentApi;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;

public class BookingRepositoryTest {
    private MockWebServer condo;
    private MockWebServer supabase;
    private BookingRepository repository;
    private final LocalDateTime start = LocalDateTime.now(ZoneId.of("Europe/Lisbon"))
            .plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
    private final LocalDateTime end = start.plusHours(1);
    private static final String RESIDENTS = "[null,{\"id\":9,\"unitId\":1,\"isActive\":true,\"firstName\":\"Ana\"},"
            + "{\"id\":10,\"unitId\":2,\"isActive\":true},{\"id\":11,\"unitId\":1,\"isActive\":false}]";
    private static final String AMENITY = "{\"id\":3,\"buildingId\":1,\"name\":\"Sala\",\"isActive\":true,"
            + "\"capacity\":10,\"openingTime\":\"08:00:00\",\"closingTime\":\"20:00:00\",\"bookingFee\":2.5}";

    @Before
    public void setup() throws Exception {
        condo = new MockWebServer();
        supabase = new MockWebServer();
        condo.start();
        supabase.start();
        Retrofit condoClient = retrofit(condo, "/api/");
        Retrofit supabaseClient = retrofit(supabase, "/rest/v1/");
        repository = new BookingRepository(supabaseClient.create(BookingApi.class),
                condoClient.create(AmenityApi.class), condoClient.create(ResidentApi.class));
    }

    private Retrofit retrofit(MockWebServer server, String path) {
        return new Retrofit.Builder().baseUrl(server.url(path))
                .addConverterFactory(GsonConverterFactory.create()).build();
    }

    @After
    public void teardown() throws Exception {
        repository.cancel();
        condo.shutdown();
        supabase.shutdown();
    }

    private void enqueue(MockWebServer server, String body) {
        server.enqueue(new MockResponse().setBody(body));
    }

    private void enqueueRulesAndResident() {
        enqueue(condo, AMENITY);
        enqueue(condo, RESIDENTS);
    }

    private String booking(long id, long unitId, long residentId, String from, String until, String status) {
        return "{\"id\":" + id + ",\"unit_id\":" + unitId + ",\"amenity_id\":3,\"resident_id\":" + residentId
                + ",\"start_time\":\"" + from + "\",\"end_time\":\"" + until + "\",\"status\":\"" + status
                + "\",\"number_of_people\":2,\"fee\":2.5}";
    }

    private String savedBooking() {
        return "[" + booking(25, 1, 9, start.toString(), end.toString(), "confirmed") + "]";
    }

    private Result<Booking> create() throws Exception {
        Result<Booking> result = new Result<>();
        repository.create(1, 3, 9, start, end, 2, "  teste  ", result);
        result.await();
        return result;
    }

    @Test
    public void readsOnlyActiveResidentsOfHomeFromTheCondo() throws Exception {
        enqueue(condo, RESIDENTS);
        Result<List<Resident>> result = new Result<>();
        repository.loadResidents(1, result);
        result.await();
        assertNull(result.error);
        assertEquals(1, result.data.size());
        assertEquals(9, result.data.get(0).getId());
        assertEquals("Ana", result.data.get(0).getFirstName());
        assertEquals("/api/residents/active", condo.takeRequest().getPath());
        assertEquals(0, supabase.getRequestCount());
    }

    @Test
    public void readsRulesFromTheCondoButWritesOnlyToSupabaseWithSnakeCaseContract() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[]");
        enqueue(supabase, savedBooking());
        Result<Booking> result = create();
        assertNull(result.error);
        assertEquals(25, result.data.getId());
        assertEquals(1, result.data.getUnitId());
        assertEquals("confirmed", result.data.getStatus());
        assertEquals(2.5, result.data.getFee(), 0.0001);
        assertEquals("/api/amenities/3", condo.takeRequest().getPath());
        assertEquals("/api/residents/active", condo.takeRequest().getPath());
        assertEquals(2, condo.getRequestCount());
        assertEquals("/rest/v1/bookings?select=amenity_id,start_time,end_time,status&status=in.(confirmed,pending)&amenity_id=eq.3", supabase.takeRequest().getPath());
        RecordedRequest request = supabase.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/rest/v1/bookings", request.getPath());
        JsonObject json = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertFalse(json.has("id"));
        assertFalse(json.has("amenityId"));
        assertFalse(json.has("p_booking"));
        assertEquals("return=representation", request.getHeader("Prefer"));
        assertEquals(1, json.get("unit_id").getAsLong());
        assertEquals(9, json.get("resident_id").getAsLong());
        assertEquals(3, json.get("amenity_id").getAsLong());
        assertEquals(start.toString(), json.get("start_time").getAsString());
        assertEquals(end.toString(), json.get("end_time").getAsString());
        assertEquals(2, json.get("number_of_people").getAsInt());
        assertEquals("confirmed", json.get("status").getAsString());
        assertEquals("teste", json.get("notes").getAsString());
        assertEquals(2.5, json.get("fee").getAsDouble(), 0.0001);
    }

    @Test
    public void refusesResidentOfAnotherHomeWithoutWritingToEitherServer() throws Exception {
        enqueue(condo, AMENITY);
        enqueue(condo, "[{\"id\":9,\"unitId\":2,\"isActive\":true}]");
        assertEquals(DataError.RESIDENT_REQUIRED, create().error);
        assertEquals(2, condo.getRequestCount());
        assertEquals(0, supabase.getRequestCount());
    }

    @Test
    public void refreshesCapacityBeforeConsultingOrWritingBookings() throws Exception {
        enqueue(condo, AMENITY.replace("\"capacity\":10", "\"capacity\":1"));
        assertEquals(DataError.INVALID_GUEST_COUNT, create().error);
        assertEquals(1, condo.getRequestCount());
        assertEquals(0, supabase.getRequestCount());
    }

    @Test
    public void overlappingBookingOfAnotherHomeInSupabaseBlocksThePeriod() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[" + booking(1, 2, 10, start.minusMinutes(15).toString(), end.toString(), "PENDING") + "]");
        assertEquals(DataError.BOOKING_CONFLICT, create().error);
        assertEquals(1, supabase.getRequestCount());
    }

    @Test
    public void availabilityNeedsOnlyTimesWithoutResidentOrContactDetails() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[{\"amenity_id\":3,\"start_time\":\"" + start
                + "\",\"end_time\":\"" + end + "\",\"status\":\"confirmed\"}]");
        assertEquals(DataError.BOOKING_CONFLICT, create().error);
    }

    @Test
    public void allowsCancelledOverlapAndAdjacentConfirmedBooking() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[" + booking(1, 2, 10, start.toString(), end.toString(), "cancelled") + ","
                + booking(2, 2, 10, end.toString(), end.plusHours(1).toString(), "confirmed") + "]");
        enqueue(supabase, savedBooking());
        assertNull(create().error);
        assertEquals(2, supabase.getRequestCount());
    }

    @Test
    public void malformedExistingTimeDoesNotBecomeFalseAvailability() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[" + booking(1, 2, 10, "invalid", end.toString(), "confirmed") + "]");
        assertEquals(DataError.INVALID_RESPONSE, create().error);
        assertEquals(1, supabase.getRequestCount());
    }

    @Test
    public void databaseConflictAfterPreflightIsReportedAsConflict() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[]");
        supabase.enqueue(new MockResponse().setResponseCode(409).setBody("{\"code\":\"23P01\"}"));
        assertEquals(DataError.BOOKING_CONFLICT, create().error);
    }

    @Test
    public void postFailureDoesNotReportSuccessOrFallBackToTheCondo() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[]");
        supabase.enqueue(new MockResponse().setResponseCode(503));
        assertEquals(DataError.BOOKING_FAILED, create().error);
        assertEquals(2, condo.getRequestCount());
        assertEquals(2, supabase.getRequestCount());
    }

    @Test
    public void homeListDoesNotDependOnResidentsRemainingActiveAndAddsSpaceName() throws Exception {
        enqueue(supabase, "[" + booking(1, 1, 11, start.toString(), end.toString(), "confirmed") + "]");
        enqueue(condo, "[" + AMENITY + "]");
        Result<List<Booking>> result = new Result<>();
        repository.loadForHome(1, result);
        result.await();
        assertNull(result.error);
        assertEquals(1, result.data.size());
        assertEquals("Sala", result.data.get(0).getAmenityName());
        assertEquals(11, result.data.get(0).getResidentId());
        assertEquals("/rest/v1/bookings?unit_id=eq.1", supabase.takeRequest().getPath());
        assertEquals("/api/amenities", condo.takeRequest().getPath());
        assertEquals(1, condo.getRequestCount());
    }

    @Test
    public void wrongHomeInResponseIsRejectedInsteadOfDisplayed() throws Exception {
        enqueue(supabase, "[" + booking(1, 2, 9, start.toString(), end.toString(), "confirmed") + "]");
        Result<List<Booking>> result = new Result<>();
        repository.loadForHome(1, result);
        result.await();
        assertEquals(DataError.INVALID_RESPONSE, result.error);
        assertEquals(0, condo.getRequestCount());
    }

    @Test
    public void emptyHomeListDoesNotQueryTheCondo() throws Exception {
        enqueue(supabase, "[]");
        Result<List<Booking>> result = new Result<>();
        repository.loadForHome(1, result);
        result.await();
        assertNull(result.error);
        assertTrue(result.data.isEmpty());
        assertEquals(0, condo.getRequestCount());
    }

    @Test
    public void missingBookingsTableShowsServiceUnavailableWithoutFallback() throws Exception {
        enqueueRulesAndResident();
        supabase.enqueue(new MockResponse().setResponseCode(404));
        assertEquals(DataError.BOOKING_SERVICE_UNAVAILABLE, create().error);
        assertEquals(2, condo.getRequestCount());
        assertEquals(1, supabase.getRequestCount());
    }

    @Test
    public void missingCreateEndpointIsNotReportedAsSaved() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[]");
        supabase.enqueue(new MockResponse().setResponseCode(404));
        assertEquals(DataError.BOOKING_SERVICE_UNAVAILABLE, create().error);
    }

    @Test
    public void missingResponseIsAnErrorInsteadOfEmptyResidentsList() throws Exception {
        condo.enqueue(new MockResponse().setResponseCode(204));
        Result<List<Resident>> result = new Result<>();
        repository.loadResidents(1, result);
        result.await();
        assertEquals(DataError.INVALID_RESPONSE, result.error);
    }

    @Test
    public void mismatchedSavedPeriodDoesNotShowSuccess() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[]");
        enqueue(supabase, "[" + booking(25, 1, 9, start.plusHours(1).toString(), end.plusHours(1).toString(), "confirmed") + "]");
        assertEquals(DataError.BOOKING_FAILED, create().error);
    }

    @Test
    public void emptyCreateResponseDoesNotShowSuccess() throws Exception {
        enqueueRulesAndResident();
        enqueue(supabase, "[]");
        enqueue(supabase, "[]");
        assertEquals(DataError.BOOKING_FAILED, create().error);
    }

    @Test
    public void requestCancellationSuppressesLateCallbacks() throws Exception {
        condo.enqueue(new MockResponse().setBody(RESIDENTS).setBodyDelay(300, TimeUnit.MILLISECONDS));
        Result<List<Resident>> result = new Result<>();
        repository.loadResidents(1, result);
        assertNotNull(condo.takeRequest(2, TimeUnit.SECONDS));
        repository.cancel();
        assertFalse(result.latch.await(600, TimeUnit.MILLISECONDS));
    }

    @Test
    public void deletionUsesDeleteWithHomeAndBookingFilters() throws Exception {
        enqueue(supabase, "[" + booking(25, 1, 9, start.toString(), end.toString(), "cancelled") + "]");
        Result<Long> result = new Result<>();
        repository.deleteBooking(1, 25, result);
        result.await();
        assertNull(result.error);
        assertEquals(Long.valueOf(25), result.data);
        assertEquals(0, condo.getRequestCount());
        RecordedRequest request = supabase.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/rest/v1/bookings?select=id,unit_id&id=eq.25&unit_id=eq.1", request.getPath());
        assertEquals("return=representation", request.getHeader("Prefer"));
        assertEquals(0, request.getBodySize());
    }

    @Test
    public void deletionCannotAcceptAResponseFromAnotherHome() throws Exception {
        enqueue(supabase, "[" + booking(25, 2, 9, start.toString(), end.toString(), "cancelled") + "]");
        Result<Long> result = new Result<>();
        repository.deleteBooking(1, 25, result);
        result.await();
        assertEquals(DataError.BOOKING_DELETE_FAILED, result.error);
    }

    @Test
    public void emptyDeletionResponseMeansBookingNotFound() throws Exception {
        enqueue(supabase, "[]");
        Result<Long> result = new Result<>();
        repository.deleteBooking(1, 25, result);
        result.await();
        assertEquals(DataError.NOT_FOUND, result.error);
    }

    @Test
    public void deletionFailureDoesNotBecomeSuccess() throws Exception {
        supabase.enqueue(new MockResponse().setResponseCode(503));
        Result<Long> result = new Result<>();
        repository.deleteBooking(1, 25, result);
        result.await();
        assertEquals(DataError.BOOKING_DELETE_FAILED, result.error);
        assertNull(result.data);
        assertEquals(0, condo.getRequestCount());
    }

    @Test
    public void missingBookingsTableIsReportedAsUnavailable() throws Exception {
        supabase.enqueue(new MockResponse().setResponseCode(404));
        Result<Long> result = new Result<>();
        repository.deleteBooking(1, 25, result);
        result.await();
        assertEquals(DataError.BOOKING_SERVICE_UNAVAILABLE, result.error);
    }

    @Test
    public void malformedDatesInHomeListAreRejectedBeforePresentation() throws Exception {
        enqueue(supabase, "[" + booking(1, 1, 9, "invalid", end.toString(), "confirmed") + "]");
        Result<List<Booking>> result = new Result<>();
        repository.loadForHome(1, result);
        result.await();
        assertEquals(DataError.INVALID_RESPONSE, result.error);
        assertEquals(0, condo.getRequestCount());
    }

    @Test
    public void invalidGuestCountInHomeListIsRejected() throws Exception {
        enqueue(supabase, "[" + booking(1, 1, 9, start.toString(), end.toString(), "confirmed")
                .replace("\"number_of_people\":2", "\"number_of_people\":0") + "]");
        Result<List<Booking>> result = new Result<>();
        repository.loadForHome(1, result);
        result.await();
        assertEquals(DataError.INVALID_RESPONSE, result.error);
    }

    @Test
    public void deletionRejectsAnUnexpectedBookingIdentifier() throws Exception {
        enqueue(supabase, "[" + booking(26, 1, 9, start.toString(), end.toString(), "confirmed") + "]");
        Result<Long> result = new Result<>();
        repository.deleteBooking(1, 25, result);
        result.await();
        assertEquals(DataError.BOOKING_DELETE_FAILED, result.error);
    }

    private static class Result<T> implements RepositoryCallback<T> {
        final CountDownLatch latch = new CountDownLatch(1);
        T data;
        DataError error;

        @Override
        public void onSuccess(T result) { data = result; latch.countDown(); }
        @Override
        public void onError(DataError failure) { error = failure; latch.countDown(); }
        void await() throws Exception { assertTrue("Repository did not finish", latch.await(5, TimeUnit.SECONDS)); }
    }
}
