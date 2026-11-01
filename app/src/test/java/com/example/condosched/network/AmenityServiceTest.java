package com.example.condosched.network;

import com.example.condosched.domain.DTO.AmenityDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class AmenityServiceTest {
    private MockWebServer server;
    private AmenityService service;

    @Before public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        AmenityApi api = new Retrofit.Builder().baseUrl(server.url("/api/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(AmenityApi.class);
        service = new AmenityService(api);
    }

    @After public void teardown() throws Exception { server.shutdown(); }

    @Test public void filtersByCondominiumAndMapsRealApiTimeAndDecimalFee() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("[{\"id\":1,\"buildingId\":7,"
                + "\"isActive\":true,\"name\":\"Piscina\",\"capacity\":50,"
                + "\"openingTime\":\"08:00:00\",\"closingTime\":\"20:00:00\","
                + "\"bookingFee\":2.5,\"requiresBooking\":true}]"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertFalse(result.error);
        assertEquals(1, result.amenities.size());
        AmenityDTO amenity = result.amenities.get(0);
        assertEquals("08:00:00", amenity.getOpeningTime());
        assertEquals("20:00:00", amenity.getClosingTime());
        assertEquals(2.5, amenity.getBookingFee(), 0.001);
        assertTrue(amenity.isRequiresBooking());
        assertEquals("/api/buildings/condominium/7", server.takeRequest().getPath());
        assertEquals("/api/amenities?condominiumId=7", server.takeRequest().getPath());
    }

    @Test public void includesOtherBuildingsInSameCondominiumButExcludesOtherCondominiums() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("[null,"
                + "{\"id\":1,\"buildingId\":7,\"isActive\":true},"
                + "{\"id\":2,\"buildingId\":7,\"isActive\":false},"
                + "{\"id\":3,\"buildingId\":8,\"isActive\":true},"
                + "{\"id\":4,\"buildingId\":9,\"isActive\":true}]"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertFalse(result.error);
        assertEquals(2, result.amenities.size());
        assertEquals(1, result.amenities.get(0).getId());
        assertEquals(3, result.amenities.get(1).getId());
        assertEquals("Building B", result.buildingNames.get(8L));
    }

    @Test public void emptyListIsSuccessful() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("[]"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertFalse(result.error);
        assertTrue(result.amenities.isEmpty());
    }

    @Test public void serverErrorIsNotAnEmptyList() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setResponseCode(503).setBody("{}"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertTrue(result.error);
        assertNull(result.amenities);
    }

    @Test public void malformedResponseReportsAnError() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("not JSON"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertTrue(result.error);
    }

    @Test public void missingCondominiumNeverRequestsAllAmenities() throws Exception {
        Result result = new Result();
        service.getByCondominium(0, result);
        result.await();
        assertTrue(result.error);
        assertEquals(0, server.getRequestCount());
    }

    private void enqueueBuildings() {
        server.enqueue(new MockResponse().setBody("["
                + "{\"id\":7,\"condominiumId\":7,\"name\":\"Building A\"},"
                + "{\"id\":8,\"condominiumId\":7,\"name\":\"Building B\"},"
                + "{\"id\":9,\"condominiumId\":99,\"name\":\"Other condominium\"}]"));
    }
    private static class Result implements AmenityService.AmenityCallback {
        final CountDownLatch done = new CountDownLatch(1);
        List<AmenityDTO> amenities;
        Map<Long, String> buildingNames;
        boolean error;
        @Override public void onSuccess(List<AmenityDTO> amenities, Map<Long, String> buildingNames) {
            this.amenities = amenities;
            this.buildingNames = buildingNames;
            done.countDown();
        }
        @Override public void onError() { error = true; done.countDown(); }
        void await() throws InterruptedException {
            assertTrue("The amenities request did not finish", done.await(5, TimeUnit.SECONDS));
        }
    }
}
